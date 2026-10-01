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
     * Generates a 384-dot wide ultra-compact high-density Bitmap for 58mm thermal printers.
     * Line heights are tightly compressed and unnecessary headers are stripped to conserve paper.
     */
    fun create58mmReceiptBitmap(
        pageName: String,
        rightItems: List<GroceryItemEntity>,
        leftItems: List<GroceryItemEntity>
    ): Bitmap {
        val width = 384 // 58mm printer width at 203 DPI is 384 dots
        val rowHeight = 22
        val headerHeight = 54
        val maxItems = maxOf(rightItems.size, leftItems.size, 1)
        val height = headerHeight + (maxItems * rowHeight) + 30

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        val compactTitlePaint = Paint().apply {
            color = Color.BLACK
            textSize = 17f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

        val metaPaint = Paint().apply {
            color = Color.BLACK
            textSize = 12f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT
            isAntiAlias = true
        }

        val columnHeaderPaint = Paint().apply {
            color = Color.BLACK
            textSize = 13f
            isFakeBoldText = true
            textAlign = Paint.Align.RIGHT
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

        val numHeaderPaint = Paint().apply {
            color = Color.BLACK
            textSize = 12f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

        val textPaint = Paint().apply {
            color = Color.BLACK
            textSize = 12f
            textAlign = Paint.Align.RIGHT
            typeface = Typeface.DEFAULT
            isAntiAlias = true
        }

        val numPaint = Paint().apply {
            color = Color.BLACK
            textSize = 12f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

        val solidLinePaint = Paint().apply {
            color = Color.BLACK
            strokeWidth = 1.5f
        }

        val thinLinePaint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 0.8f
        }

        var currentY = 18f

        // 1. Single Compact Header: Store Name & Page Name
        canvas.drawText("بقالة العزي • $pageName", width / 2f, currentY, compactTitlePaint)
        currentY += 15f

        // 2. Compact Meta Line: Date and Total Items
        val dateFormat = SimpleDateFormat("yy/MM/dd HH:mm", Locale.getDefault())
        val dateStr = dateFormat.format(Date())
        val totalCount = rightItems.size + leftItems.size
        canvas.drawText("$dateStr • إجمالي: $totalCount صنف", width / 2f, currentY, metaPaint)
        currentY += 8f

        // Top Divider Line
        canvas.drawLine(8f, currentY, (width - 8).toFloat(), currentY, solidLinePaint)
        currentY += 13f

        // 3. Compact Column Headers: [الشق الأيمن] and [الشق الأيسر]
        val colWidth = width / 2f
        canvas.drawText("أيمن 🔷", (width - 12).toFloat(), currentY, columnHeaderPaint)
        canvas.drawText("عدد", (width - colWidth + 18f), currentY, numHeaderPaint)

        canvas.drawText("أيسر 🟢", (colWidth - 12), currentY, columnHeaderPaint)
        canvas.drawText("عدد", 20f, currentY, numHeaderPaint)
        currentY += 5f

        val tableTopY = currentY
        canvas.drawLine(8f, currentY, (width - 8).toFloat(), currentY, solidLinePaint)
        currentY += 15f

        // 4. Compact Table Rows
        for (i in 0 until maxItems) {
            val rightItem = rightItems.getOrNull(i)
            val leftItem = leftItems.getOrNull(i)

            if (rightItem != null) {
                val truncatedName = if (rightItem.name.length > 13) rightItem.name.take(12) + "…" else rightItem.name
                canvas.drawText(truncatedName, (width - 12).toFloat(), currentY, textPaint)
                canvas.drawText("${rightItem.qty}", (width - colWidth + 18f), currentY, numPaint)
            }

            if (leftItem != null) {
                val truncatedName = if (leftItem.name.length > 13) leftItem.name.take(12) + "…" else leftItem.name
                canvas.drawText(truncatedName, (colWidth - 12), currentY, textPaint)
                canvas.drawText("${leftItem.qty}", 20f, currentY, numPaint)
            }

            currentY += 18f
            // Thin horizontal line between rows
            canvas.drawLine(8f, currentY - 5f, (width - 8).toFloat(), currentY - 5f, thinLinePaint)
        }

        // Vertical Center Divider Line
        canvas.drawLine(colWidth, tableTopY, colWidth, currentY - 5f, solidLinePaint)

        // Bottom border line
        canvas.drawLine(8f, currentY - 5f, (width - 8).toFloat(), currentY - 5f, solidLinePaint)

        return bitmap
    }

    /** 384-dot ESC/POS bitmap: compact, monochrome, no emoji, no arbitrary text clipping. */
    fun create58mmReceiptBitmap(pageName: String, rightItems: List<GroceryItemEntity>, leftItems: List<GroceryItemEntity>): Bitmap {
        val width = 384; val rowHeight = 21; val top = 48
        val maxItems = maxOf(rightItems.size, leftItems.size, 1)
        val height = top + (maxItems * rowHeight) + 14
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        val canvas = Canvas(bitmap); canvas.drawColor(Color.WHITE)
        fun paint(size: Float, bold: Boolean, align: Paint.Align) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK; textSize = size; textAlign = align; typeface = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        }
        val title=paint(16f,true,Paint.Align.CENTER); val meta=paint(10f,false,Paint.Align.CENTER)
        val header=paint(11f,true,Paint.Align.RIGHT); val qty=paint(11f,true,Paint.Align.CENTER); val name=paint(10.5f,true,Paint.Align.RIGHT)
        val line=Paint().apply { color=Color.BLACK; strokeWidth=1f }; val thin=Paint().apply { color=Color.LTGRAY; strokeWidth=.6f }
        var y=15f
        canvas.drawText("بقالة العزي - " + pageName, width/2f, y, title); y+=13f
        val date=SimpleDateFormat("yy/MM/dd HH:mm",Locale.getDefault()).format(Date())
        canvas.drawText(date + "  |  " + (rightItems.size+leftItems.size) + " صنف", width/2f, y, meta); y+=6f
        canvas.drawLine(6f,y,width-6f,y,line); y+=13f
        val mid=width/2f
        canvas.drawText("الشق الأيمن",width-8f,y,header); canvas.drawText("العدد",mid-24f,y,qty)
        canvas.drawText("الشق الأيسر",mid-8f,y,header); canvas.drawText("العدد",24f,y,qty); y+=4f
        canvas.drawLine(6f,y,width-6f,y,line); val tableTop=y; y+=14f
        fun drawItem(item:GroceryItemEntity?, nameRight:Float, qtyX:Float) {
            if(item==null) return
            val fittedCount=name.breakText(item.name,true,118f,null).coerceAtLeast(1)
            val fitted=item.name.take(fittedCount)
            canvas.drawText(fitted,nameRight,y,name); canvas.drawText(item.qty.toString(),qtyX,y,qty)
        }
        for(i in 0 until maxItems) {
            drawItem(rightItems.getOrNull(i),width-8f,mid-24f); drawItem(leftItems.getOrNull(i),mid-8f,24f)
            canvas.drawLine(8f,y+5f,width-8f,y+5f,thin); y+=rowHeight
        }
        canvas.drawLine(mid,tableTop,mid,y-16f,line); canvas.drawLine(6f,y-16f,width-6f,y-16f,line)
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

        // 5. Feed 2 lines and partial cut: ESC d 2
        output.write(byteArrayOf(0x1B, 0x64, 0x02))

        return output.toByteArray()
    }
}
