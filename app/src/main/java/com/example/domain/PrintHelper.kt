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
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.content.FileProvider
import com.example.data.local.GroceryItemEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PrintHelper {

    /**
     * Prints or exports to PDF using Android PrintManager and a hidden WebView.
     */
    fun printDocument(activity: Activity, htmlContent: String, jobTitle: String = "كشف_نواقص_بقالة_العزي") {
        activity.runOnUiThread {
            val webView = WebView(activity)
            webView.webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean = false

                override fun onPageFinished(view: WebView?, url: String?) {
                    val printManager = activity.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                    val printAdapter = webView.createPrintDocumentAdapter(jobTitle)
                    printManager?.print(
                        jobTitle,
                        printAdapter,
                        PrintAttributes.Builder()
                            .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                            .setColorMode(PrintAttributes.COLOR_MODE_COLOR)
                            .build()
                    )
                }
            }
            webView.loadDataWithBaseURL(null, htmlContent, "text/html; charset=utf-8", "UTF-8", null)
        }
    }

    /**
     * Generates a high-resolution branded summary image (PNG) and returns Uri or shares directly.
     */
    fun shareDualColumnAsImage(
        context: Context,
        pageName: String,
        rightItems: List<GroceryItemEntity>,
        leftItems: List<GroceryItemEntity>
    ) {
        val width = 1080
        val maxRows = maxOf(rightItems.size, leftItems.size, 1)
        val rowHeight = 56
        val headerHeight = 360
        val height = headerHeight + (maxRows * rowHeight) + 160

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Dark POS background
        canvas.drawColor(android.graphics.Color.rgb(3, 7, 18)) // #030712

        val bgPaint = Paint().apply { isAntiAlias = true }

        // Top Header Banner
        val headerRect = RectF(24f, 24f, (width - 24).toFloat(), 200f)
        bgPaint.color = android.graphics.Color.rgb(15, 23, 42) // Slate 900
        canvas.drawRoundRect(headerRect, 20f, 20f, bgPaint)

        // Title
        val titlePaint = Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 42f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }
        canvas.drawText("🏪 بقالة العزي للمواد الغذائية", width / 2f, 90f, titlePaint)

        val subTitlePaint = Paint().apply {
            color = android.graphics.Color.rgb(245, 158, 11) // Amber #f59e0b
            textSize = 28f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }
        canvas.drawText("كشف النواقص والطلبيات المزدوج - $pageName", width / 2f, 136f, subTitlePaint)

        val dateFormat = SimpleDateFormat("yyyy/MM/dd - hh:mm a", Locale("ar"))
        val datePaint = Paint().apply {
            color = android.graphics.Color.rgb(148, 163, 184) // Slate 400
            textSize = 22f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT
            isAntiAlias = true
        }
        canvas.drawText("التاريخ: ${dateFormat.format(Date())} | إجمالي الأصناف: ${rightItems.size + leftItems.size}", width / 2f, 175f, datePaint)

        // Dual Columns
        val margin = 24f
        val gap = 20f
        val availableWidth = width - (margin * 2) - gap
        val colWidth = availableWidth / 2f

        val rightColLeft = margin + colWidth + gap
        val rightColRight = width - margin

        val leftColLeft = margin
        val leftColRight = margin + colWidth

        val tableTop = 230f
        val tableBottom = (tableTop + 60f + (maxRows * rowHeight)).toFloat()

        // Right Column Card (Royal Blue)
        val rightCardRect = RectF(rightColLeft, tableTop, rightColRight, tableBottom)
        bgPaint.color = android.graphics.Color.rgb(11, 25, 44) // Deep blue tint
        canvas.drawRoundRect(rightCardRect, 16f, 16f, bgPaint)

        val rightHeaderRect = RectF(rightColLeft, tableTop, rightColRight, tableTop + 55f)
        bgPaint.color = android.graphics.Color.rgb(37, 99, 235) // Royal blue
        canvas.drawRoundRect(rightHeaderRect, 16f, 16f, bgPaint)

        // Left Column Card (Emerald Green)
        val leftCardRect = RectF(leftColLeft, tableTop, leftColRight, tableBottom)
        bgPaint.color = android.graphics.Color.rgb(6, 35, 25) // Deep emerald tint
        canvas.drawRoundRect(leftCardRect, 16f, 16f, bgPaint)

        val leftHeaderRect = RectF(leftColLeft, tableTop, leftColRight, tableTop + 55f)
        bgPaint.color = android.graphics.Color.rgb(5, 150, 105) // Emerald
        canvas.drawRoundRect(leftHeaderRect, 16f, 16f, bgPaint)

        val colTitlePaint = Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 26f
            isFakeBoldText = true
            textAlign = Paint.Align.RIGHT
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

        val badgePaint = Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 22f
            textAlign = Paint.Align.LEFT
            typeface = Typeface.DEFAULT
            isAntiAlias = true
        }

        canvas.drawText("🔷 الشق الأيمن", rightColRight - 20f, tableTop + 38f, colTitlePaint)
        canvas.drawText("(${rightItems.size})", rightColLeft + 20f, tableTop + 38f, badgePaint)

        canvas.drawText("🟢 الشق الأيسر", leftColRight - 20f, tableTop + 38f, colTitlePaint)
        canvas.drawText("(${leftItems.size})", leftColLeft + 20f, tableTop + 38f, badgePaint)

        // Draw items
        val itemTextPaint = Paint().apply {
            color = android.graphics.Color.rgb(241, 245, 249)
            textSize = 22f
            textAlign = Paint.Align.RIGHT
            typeface = Typeface.DEFAULT
            isAntiAlias = true
        }

        val rightQtyPaint = Paint().apply {
            color = android.graphics.Color.rgb(245, 158, 11) // Amber
            textSize = 24f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

        val leftQtyPaint = Paint().apply {
            color = android.graphics.Color.rgb(52, 211, 153) // Light emerald
            textSize = 24f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

        val dividerPaint = Paint().apply {
            color = android.graphics.Color.rgb(30, 41, 59)
            strokeWidth = 1.5f
        }

        var rowY = tableTop + 95f
        for (i in 0 until maxRows) {
            val rItem = rightItems.getOrNull(i)
            val lItem = leftItems.getOrNull(i)

            if (rItem != null) {
                canvas.drawText(rItem.name, rightColRight - 20f, rowY, itemTextPaint)
                canvas.drawText("${rItem.qty}", rightColLeft + 45f, rowY, rightQtyPaint)
            }
            if (lItem != null) {
                canvas.drawText(lItem.name, leftColRight - 20f, rowY, itemTextPaint)
                canvas.drawText("${lItem.qty}", leftColLeft + 45f, rowY, leftQtyPaint)
            }

            canvas.drawLine(rightColLeft + 15f, rowY + 16f, rightColRight - 15f, rowY + 16f, dividerPaint)
            canvas.drawLine(leftColLeft + 15f, rowY + 16f, leftColRight - 15f, rowY + 16f, dividerPaint)
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
