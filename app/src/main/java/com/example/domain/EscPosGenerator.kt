package com.example.domain

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import com.example.data.local.GroceryItemEntity
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object EscPosGenerator {

    /**
     * Generates a 384-dot wide high-contrast monochrome Bitmap for 58mm thermal printers,
     * fully supporting Arabic shaping and dual columns without character corruption.
     */
    fun create58mmReceiptBitmap(
        pageName: String,
        rightItems: List<GroceryItemEntity>,
        leftItems: List<GroceryItemEntity>
    ): Bitmap {
        val width = 384 // 58mm printer width at 203 DPI is 384 dots
        val rowHeight = 32
        val headerHeight = 160
        val maxItems = maxOf(rightItems.size, leftItems.size, 1)
        val height = headerHeight + (maxItems * rowHeight) + 120

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        val titlePaint = Paint().apply {
            color = Color.BLACK
            textSize = 22f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

        val subtitlePaint = Paint().apply {
            color = Color.BLACK
            textSize = 15f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT
            isAntiAlias = true
        }

        val boldHeaderPaint = Paint().apply {
            color = Color.BLACK
            textSize = 14f
            isFakeBoldText = true
            textAlign = Paint.Align.RIGHT
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

        val textPaint = Paint().apply {
            color = Color.BLACK
            textSize = 13f
            textAlign = Paint.Align.RIGHT
            typeface = Typeface.DEFAULT
            isAntiAlias = true
        }

        val numPaint = Paint().apply {
            color = Color.BLACK
            textSize = 13f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

        val linePaint = Paint().apply {
            color = Color.BLACK
            strokeWidth = 2f
        }

        val dashedLinePaint = Paint().apply {
            color = Color.DKGRAY
            strokeWidth = 1f
        }

        var currentY = 32f

        // Store Header
        canvas.drawText("بقالة العزي للمواد الغذائية", width / 2f, currentY, titlePaint)
        currentY += 24f
        canvas.drawText("كشف نواقص - $pageName", width / 2f, currentY, subtitlePaint)
        currentY += 22f

        val dateFormat = SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale("ar"))
        val dateStr = dateFormat.format(Date())
        canvas.drawText("التاريخ: $dateStr", width / 2f, currentY, subtitlePaint)
        currentY += 18f
        canvas.drawText("إجمالي الأصناف: ${rightItems.size + leftItems.size}", width / 2f, currentY, subtitlePaint)
        currentY += 16f

        // Separator
        canvas.drawLine(10f, currentY, (width - 10).toFloat(), currentY, linePaint)
        currentY += 20f

        // Column Titles: Right Split (Blue section) and Left Split (Green section)
        // Note: In RTL, right half is 192..384, left half is 0..192
        val colWidth = width / 2f
        canvas.drawText("[الشق الأيمن]", (width - 15).toFloat(), currentY, boldHeaderPaint)
        canvas.drawText("[الشق الأيسر]", (colWidth - 15), currentY, boldHeaderPaint)
        currentY += 18f

        // Table sub-headers
        canvas.drawText("العدد", (width - colWidth + 25f), currentY, numPaint)
        canvas.drawText("الصنف", (width - 15).toFloat(), currentY, boldHeaderPaint)

        canvas.drawText("العدد", 25f, currentY, numPaint)
        canvas.drawText("الصنف", (colWidth - 15), currentY, boldHeaderPaint)
        currentY += 10f

        canvas.drawLine(10f, currentY, (width - 10).toFloat(), currentY, linePaint)
        // Center divider line
        val tableTopY = currentY
        currentY += 18f

        for (i in 0 until maxItems) {
            val rightItem = rightItems.getOrNull(i)
            val leftItem = leftItems.getOrNull(i)

            if (rightItem != null) {
                // Shorten name if too long for 58mm half-column
                val truncatedName = if (rightItem.name.length > 14) rightItem.name.take(13) + "…" else rightItem.name
                canvas.drawText(truncatedName, (width - 15).toFloat(), currentY, textPaint)
                canvas.drawText("${rightItem.qty}", (width - colWidth + 25f), currentY, numPaint)
            }

            if (leftItem != null) {
                val truncatedName = if (leftItem.name.length > 14) leftItem.name.take(13) + "…" else leftItem.name
                canvas.drawText(truncatedName, (colWidth - 15), currentY, textPaint)
                canvas.drawText("${leftItem.qty}", 25f, currentY, numPaint)
            }

            currentY += 22f
            canvas.drawLine(10f, currentY - 6f, (width - 10).toFloat(), currentY - 6f, dashedLinePaint)
        }

        // Draw vertical column divider
        canvas.drawLine(colWidth, tableTopY, colWidth, currentY - 6f, linePaint)

        currentY += 18f
        canvas.drawLine(10f, currentY, (width - 10).toFloat(), currentY, linePaint)
        currentY += 22f

        canvas.drawText("شكراً لتعاملكم مع بقالة العزي", width / 2f, currentY, subtitlePaint)

        return bitmap
    }

    /**
     * Converts a monochrome Bitmap into standard ESC/POS GS v 0 raster byte commands.
     */
    fun bitmapToEscPosBytes(bitmap: Bitmap): ByteArray {
        val width = bitmap.width
        val height = bitmap.height
        val widthBytes = (width + 7) / 8

        val output = ByteArrayOutputStream()

        // 1. Initialize printer: ESC @ (0x1B, 0x40)
        output.write(byteArrayOf(0x1B, 0x40))

        // 2. Set line spacing to 0: ESC 3 0 (0x1B, 0x33, 0x00)
        output.write(byteArrayOf(0x1B, 0x33, 0x00))

        // 3. Raster Bit Image Command: GS v 0 m xL xH yL yH
        // m = 0 (normal)
        val xL = (widthBytes and 0xFF).toByte()
        val xH = ((widthBytes shr 8) and 0xFF).toByte()
        val yL = (height and 0xFF).toByte()
        val yH = ((height shr 8) and 0xFF).toByte()

        output.write(byteArrayOf(0x1D, 0x76, 0x30, 0x00, xL, xH, yL, yH))

        // 4. Monochrome bit data (1 = black, 0 = white)
        val threshold = 160
        for (y in 0 until height) {
            for (xByte in 0 until widthBytes) {
                var byteVal = 0
                for (b in 0 until 8) {
                    val x = xByte * 8 + b
                    if (x < width) {
                        val pixel = bitmap.getPixel(x, y)
                        val r = (pixel shr 16) and 0xFF
                        val g = (pixel shr 8) and 0xFF
                        val bl = pixel and 0xFF
                        val luminance = (0.299 * r + 0.587 * g + 0.114 * bl).toInt()
                        if (luminance < threshold) {
                            byteVal = byteVal or (1 shl (7 - b))
                        }
                    }
                }
                output.write(byteVal)
            }
        }

        // 5. Feed 4 lines and partial cut: ESC d 4
        output.write(byteArrayOf(0x1B, 0x64, 0x04))

        return output.toByteArray()
    }
}
