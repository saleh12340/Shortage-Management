package com.example.domain

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.print.pdf.PrintedPdfDocument
import androidx.core.content.FileProvider
import com.example.data.local.GroceryItemEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PrintHelper {

    /**
     * Prints the ultra-compact 58mm thermal receipt directly via Android PrintManager.
     * Configures the print job for 58mm roll paper with zero margins instead of A4.
     */
    fun printThermalReceipt(
        activity: Activity,
        receiptBitmap: Bitmap,
        pageName: String
    ) {
        val jobTitle = "إيصال_نواقص_$pageName"
        val printManager = activity.getSystemService(Context.PRINT_SERVICE) as? PrintManager ?: return

        // 58mm roll paper size in mils (1 mil = 1/1000 inch):
        // 58mm = ~2.283 inches = 2283 mils
        // Height calculated proportionally from the bitmap aspect ratio
        val widthMils = 2283
        val heightMils = ((receiptBitmap.height.toFloat() / receiptBitmap.width.toFloat()) * widthMils).toInt().coerceAtLeast(3000)

        val rollMediaSize = PrintAttributes.MediaSize(
            "ROLL_58MM",
            "إيصال حراري 58mm",
            widthMils,
            heightMils
        )

        val printAttributes = PrintAttributes.Builder()
            .setMediaSize(rollMediaSize)
            .setMinMargins(PrintAttributes.Margins(0, 0, 0, 0))
            .setColorMode(PrintAttributes.COLOR_MODE_MONOCHROME)
            .build()

        val adapter = object : PrintDocumentAdapter() {
            private var pdfDocument: PrintedPdfDocument? = null

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

                pdfDocument = PrintedPdfDocument(activity, newAttributes ?: printAttributes)

                val info = PrintDocumentInfo.Builder(jobTitle)
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(1)
                    .build()

                callback?.onLayoutFinished(info, true)
            }

            override fun onWrite(
                pages: Array<out PageRange>?,
                destination: ParcelFileDescriptor?,
                cancellationSignal: CancellationSignal?,
                callback: WriteResultCallback?
            ) {
                if (cancellationSignal?.isCanceled == true) {
                    callback?.onWriteCancelled()
                    return
                }

                val doc = pdfDocument ?: return
                val page = doc.startPage(0)

                val canvas = page.canvas
                val pageWidth = page.info.pageWidth.toFloat()
                // Fit bitmap tightly to page width with 0 wasted margin
                val scale = pageWidth / receiptBitmap.width.toFloat()
                val targetHeight = receiptBitmap.height.toFloat() * scale

                val destRect = RectF(0f, 0f, pageWidth, targetHeight)
                val paint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG)
                canvas.drawBitmap(receiptBitmap, null, destRect, paint)

                doc.finishPage(page)

                try {
                    destination?.let { pfd ->
                        FileOutputStream(pfd.fileDescriptor).use { out ->
                            doc.writeTo(out)
                        }
                    }
                    callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                } catch (e: Exception) {
                    callback?.onWriteFailed(e.message)
                } finally {
                    doc.close()
                    pdfDocument = null
                }
            }
        }

        printManager.print(jobTitle, adapter, printAttributes)
    }

    /** Send the 58mm ESC/POS raster directly to a paired Bluetooth thermal printer. */
    fun printBluetoothReceipt(context: Context, receiptBitmap: Bitmap, pageName: String, onResult: (String) -> Unit) {
        if (android.os.Build.VERSION.SDK_INT >= 31 && androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            onResult("امنح إذن Bluetooth ثم أعد الطباعة"); return
        }
        val adapter = (context.getSystemService(Context.BLUETOOTH_SERVICE) as? android.bluetooth.BluetoothManager)?.adapter
        if (adapter == null || !adapter.isEnabled) { onResult("فعّل Bluetooth أولاً"); return }
        val devices = try { adapter.bondedDevices?.toList().orEmpty() } catch (_: SecurityException) { emptyList() }
        if (devices.isEmpty()) { onResult("لا توجد طابعة Bluetooth مقترنة بالجهاز"); return }
        val names = devices.map { (it.name ?: "جهاز Bluetooth") + "\n" + it.address }.toTypedArray()
        android.app.AlertDialog.Builder(context).setTitle("اختر طابعة 58mm").setItems(names) { _, which ->
            Thread {
                var socket: android.bluetooth.BluetoothSocket? = null
                try {
                    val device = devices[which]
                    socket = device.createRfcommSocketToServiceRecord(java.util.UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"))
                    adapter.cancelDiscovery(); socket.connect()
                    socket.outputStream.use { out -> out.write(EscPosGenerator.bitmapToEscPosBytes(receiptBitmap)); out.flush() }
                    onResult("تمت طباعة الإيصال 58mm ✓")
                } catch (e: Exception) { onResult("تعذر الطباعة: " + (e.message ?: "تحقق من اقتران الطابعة")) }
                finally { try { socket?.close() } catch (_: Exception) {} }
            }.start()
        }.setNegativeButton("إلغاء", null).show()
    }
    /**
     * Generates a compact, high-density summary image optimized for WhatsApp and thermal printing.
     * Uses tight row padding and minimal header height to prevent large A4-like blank spaces.
     */
    fun shareDualColumnAsImage(
        context: Context,
        pageName: String,
        rightItems: List<GroceryItemEntity>,
        leftItems: List<GroceryItemEntity>
    ) {
        val width = 640 // High-density compact mobile width
        val maxRows = maxOf(rightItems.size, leftItems.size, 1)
        val rowHeight = 30
        val headerHeight = 90
        val height = headerHeight + (maxRows * rowHeight) + 40

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Dark POS background
        canvas.drawColor(Color.rgb(11, 19, 43)) // Dark navy slate

        val bgPaint = Paint().apply { isAntiAlias = true }

        // Top Header Banner (Ultra Compact)
        val headerRect = RectF(10f, 10f, (width - 10).toFloat(), 80f)
        bgPaint.color = Color.rgb(15, 23, 42)
        canvas.drawRoundRect(headerRect, 10f, 10f, bgPaint)

        // Store and Page Title
        val titlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 20f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }
        canvas.drawText("بقالة العزي • $pageName", width / 2f, 42f, titlePaint)

        val dateFormat = SimpleDateFormat("yy/MM/dd HH:mm", Locale.getDefault())
        val datePaint = Paint().apply {
            color = Color.rgb(245, 158, 11) // Amber
            textSize = 13f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT
            isAntiAlias = true
        }
        val totalCount = rightItems.size + leftItems.size
        canvas.drawText("${dateFormat.format(Date())} • إجمالي: $totalCount صنف", width / 2f, 68f, datePaint)

        // Dual Columns
        val margin = 10f
        val gap = 8f
        val colWidth = (width - (margin * 2) - gap) / 2f

        val rightColLeft = margin + colWidth + gap
        val rightColRight = width - margin

        val leftColLeft = margin
        val leftColRight = margin + colWidth

        val tableTop = 95f
        val tableBottom = (tableTop + 35f + (maxRows * rowHeight)).toFloat()

        // Right Column Background
        bgPaint.color = Color.rgb(15, 29, 61)
        canvas.drawRoundRect(RectF(rightColLeft, tableTop, rightColRight, tableBottom), 8f, 8f, bgPaint)

        // Left Column Background
        bgPaint.color = Color.rgb(6, 40, 30)
        canvas.drawRoundRect(RectF(leftColLeft, tableTop, leftColRight, tableBottom), 8f, 8f, bgPaint)

        // Column Titles
        val colTitlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 14f
            isFakeBoldText = true
            textAlign = Paint.Align.RIGHT
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

        val badgePaint = Paint().apply {
            color = Color.WHITE
            textSize = 13f
            textAlign = Paint.Align.LEFT
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

        // Right header
        bgPaint.color = Color.rgb(37, 99, 235)
        canvas.drawRoundRect(RectF(rightColLeft, tableTop, rightColRight, tableTop + 30f), 8f, 8f, bgPaint)
        canvas.drawText("🔷 الشق الأيمن", rightColRight - 10f, tableTop + 21f, colTitlePaint)
        canvas.drawText("${rightItems.size}", rightColLeft + 10f, tableTop + 21f, badgePaint)

        // Left header
        bgPaint.color = Color.rgb(5, 150, 105)
        canvas.drawRoundRect(RectF(leftColLeft, tableTop, leftColRight, tableTop + 30f), 8f, 8f, bgPaint)
        canvas.drawText("🟢 الشق الأيسر", leftColRight - 10f, tableTop + 21f, colTitlePaint)
        canvas.drawText("${leftItems.size}", leftColLeft + 10f, tableTop + 21f, badgePaint)

        val itemTextPaint = Paint().apply {
            color = Color.rgb(241, 245, 249)
            textSize = 13f
            textAlign = Paint.Align.RIGHT
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

        val rightQtyPaint = Paint().apply {
            color = Color.rgb(245, 158, 11) // Amber
            textSize = 14f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

        val leftQtyPaint = Paint().apply {
            color = Color.rgb(52, 211, 153) // Green
            textSize = 14f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

        val dividerPaint = Paint().apply {
            color = Color.rgb(30, 41, 59)
            strokeWidth = 1f
        }

        var rowY = tableTop + 50f
        for (i in 0 until maxRows) {
            val rItem = rightItems.getOrNull(i)
            val lItem = leftItems.getOrNull(i)

            if (rItem != null) {
                val name = if (rItem.name.length > 18) rItem.name.take(17) + "…" else rItem.name
                canvas.drawText(name, rightColRight - 10f, rowY, itemTextPaint)
                canvas.drawText("${rItem.qty}", rightColLeft + 25f, rowY, rightQtyPaint)
            }
            if (lItem != null) {
                val name = if (lItem.name.length > 18) lItem.name.take(17) + "…" else lItem.name
                canvas.drawText(name, leftColRight - 10f, rowY, itemTextPaint)
                canvas.drawText("${lItem.qty}", leftColLeft + 25f, rowY, leftQtyPaint)
            }

            canvas.drawLine(rightColLeft + 8f, rowY + 9f, rightColRight - 8f, rowY + 9f, dividerPaint)
            canvas.drawLine(leftColLeft + 8f, rowY + 9f, leftColRight - 8f, rowY + 9f, dividerPaint)
            rowY += rowHeight
        }

        // Save to cache file and share
        try {
            val cachePath = File(context.cacheDir, "images")
            cachePath.mkdirs()
            val file = File(cachePath, "ezzi_shortages_${System.currentTimeMillis()}.png")
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()

            val authority = "${context.packageName}.fileprovider"
            val contentUri: Uri = FileProvider.getUriForFile(context, authority, file)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, "كشف نواقص بقالة العزي - $pageName")
                putExtra(Intent.EXTRA_TEXT, "كشف نواقص بقالة العزي للمواد الغذائية ($pageName)")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(shareIntent, "مشاركة صورة كشف النواقص").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
