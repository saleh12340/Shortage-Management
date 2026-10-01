package com.example.domain

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.RectF
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.print.pdf.PrintedPdfDocument
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.example.data.local.GroceryItemEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PrintHelper {

    fun printThermalReceipt(activity: Activity, receiptBitmap: Bitmap, pageName: String) {
        val jobTitle = "إيصال نواقص بقالة العزي - $pageName"
        val printManager = activity.getSystemService(Context.PRINT_SERVICE) as? PrintManager ?: return
        val widthMils = 2283
        val heightMils = ((receiptBitmap.height.toFloat() / receiptBitmap.width.toFloat()) * widthMils)
            .toInt().coerceAtLeast(900)

        val attributes = PrintAttributes.Builder()
            .setMediaSize(PrintAttributes.MediaSize("AL_EZZI_58MM", "إيصال حراري 58mm", widthMils, heightMils))
            .setMinMargins(PrintAttributes.Margins(0, 0, 0, 0))
            .setColorMode(PrintAttributes.COLOR_MODE_MONOCHROME)
            .build()

        val adapter = object : PrintDocumentAdapter() {
            private var document: PrintedPdfDocument? = null

            override fun onLayout(
                oldAttributes: PrintAttributes?,
                newAttributes: PrintAttributes?,
                cancellationSignal: CancellationSignal?,
                callback: LayoutResultCallback?,
                extras: Bundle?
            ) {
                if (cancellationSignal?.isCanceled == true) {
                    callback?.onLayoutCancelled()
                    return
                }
                document = PrintedPdfDocument(activity, newAttributes ?: attributes)
                callback?.onLayoutFinished(
                    PrintDocumentInfo.Builder(jobTitle)
                        .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                        .setPageCount(1)
                        .build(),
                    true
                )
            }

            override fun onWrite(
                pages: Array<out PageRange>?,
                destination: ParcelFileDescriptor?,
                cancellationSignal: CancellationSignal?,
                callback: WriteResultCallback?
            ) {
                val doc = document
                if (doc == null || cancellationSignal?.isCanceled == true) {
                    callback?.onWriteCancelled()
                    return
                }
                try {
                    val page = doc.startPage(0)
                    val canvas = page.canvas
                    val pageWidth = page.info.pageWidth.toFloat()
                    val scale = pageWidth / receiptBitmap.width.toFloat()
                    val targetHeight = receiptBitmap.height * scale
                    canvas.drawBitmap(
                        receiptBitmap,
                        null,
                        RectF(0f, 0f, pageWidth, targetHeight),
                        Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
                    )
                    doc.finishPage(page)
                    destination?.let { pfd ->
                        FileOutputStream(pfd.fileDescriptor).use { out -> doc.writeTo(out) }
                    }
                    callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                } catch (e: Exception) {
                    callback?.onWriteFailed(e.message)
                } finally {
                    doc.close()
                    document = null
                }
            }
        }

        // Android system print preview/print service; never connects to Bluetooth directly.
        printManager.print(jobTitle, adapter, attributes)
    }

    fun saveReceiptImage(context: Context, receiptBitmap: Bitmap, pageName: String): Uri? {
        val safeName = pageName.replace(Regex("[^\\p{L}\\p{N}_-]+"), "_").trim('_').ifEmpty { "كشف" }
        val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val displayName = "بقالة_العزي_" + safeName + "_" + stamp + ".png"

        return try {
            if (Build.VERSION.SDK_INT >= 29) {
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/بقالة العزي")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return null
                try {
                    resolver.openOutputStream(uri)?.use { out ->
                        check(receiptBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) { "تعذر حفظ الصورة" }
                    } ?: error("تعذر فتح ملف الصورة")
                    values.clear()
                    values.put(MediaStore.Images.Media.IS_PENDING, 0)
                    resolver.update(uri, values, null, null)
                    uri
                } catch (e: Exception) {
                    resolver.delete(uri, null, null)
                    throw e
                }
            } else {
                val dir = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "بقالة العزي")
                    .apply { mkdirs() }
                val file = File(dir, displayName)
                FileOutputStream(file).use { out ->
                    check(receiptBitmap.compress(Bitmap.CompressFormat.PNG, 100, out))
                }
                FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)
            }
        } catch (_: Exception) {
            null
        }
    }

    fun shareReceiptAsImage(
        context: Context,
        pageName: String,
        rightItems: List<GroceryItemEntity>,
        leftItems: List<GroceryItemEntity>,
        onSaved: ((Uri) -> Unit)? = null
    ) {
        val bitmap = EscPosGenerator.create58mmReceiptBitmap(pageName, rightItems, leftItems)
        val uri = saveReceiptImage(context, bitmap, pageName) ?: return
        onSaved?.invoke(uri)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "إيصال بقالة العزي - $pageName")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, "مشاركة إيصال بقالة العزي"))
    }

    fun shareDualColumnAsImage(
        context: Context,
        pageName: String,
        rightItems: List<GroceryItemEntity>,
        leftItems: List<GroceryItemEntity>
    ) = shareReceiptAsImage(context, pageName, rightItems, leftItems)
}
