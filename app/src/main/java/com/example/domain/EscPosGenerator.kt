package com.example.domain

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import com.example.data.local.GroceryItemEntity
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object EscPosGenerator {
    private const val RECEIPT_WIDTH = 384
    private const val SIDE_PADDING = 8
    private const val COLUMN_GAP = 8
    private const val ROW_PADDING = 5
    private const val NAME_TEXT_SIZE = 13f
    private const val QTY_TEXT_SIZE = 13f

    fun create58mmReceiptBitmap(
        pageName: String,
        rightItems: List<GroceryItemEntity>,
        leftItems: List<GroceryItemEntity>
    ): Bitmap {
        val columnWidth = (RECEIPT_WIDTH - (SIDE_PADDING * 2) - COLUMN_GAP) / 2
        val nameWidth = columnWidth - 42
        val rows = maxOf(rightItems.size, leftItems.size, 1)
        val rowHeights = (0 until rows).map { index ->
            val rightHeight = wrappedHeight(rightItems.getOrNull(index)?.name, nameWidth)
            val leftHeight = wrappedHeight(leftItems.getOrNull(index)?.name, nameWidth)
            maxOf(rightHeight, leftHeight) + ROW_PADDING
        }
        val contentTop = 97
        val height = contentTop + rowHeights.sum() + 18

        val bitmap = Bitmap.createBitmap(RECEIPT_WIDTH, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap).apply { drawColor(Color.WHITE) }
        val title = paint(18f, true, Paint.Align.CENTER)
        val meta = paint(11f, false, Paint.Align.CENTER)
        val header = paint(13f, true, Paint.Align.CENTER)
        val qty = paint(QTY_TEXT_SIZE, true, Paint.Align.CENTER)
        val divider = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            strokeWidth = 1f
        }
        val rowDivider = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.LTGRAY
            strokeWidth = 1f
        }

        var y = 20f
        canvas.drawText("بقالة العزي", RECEIPT_WIDTH / 2f, y, title)
        y += 18f
        canvas.drawText(pageName.trim(), RECEIPT_WIDTH / 2f, y, meta)
        y += 15f
        val date = SimpleDateFormat("yy/MM/dd HH:mm", Locale.getDefault()).format(Date())
        canvas.drawText(
            "${date}  |  ${rightItems.size + leftItems.size} صنف",
            RECEIPT_WIDTH / 2f,
            y,
            meta
        )
        y += 9f
        canvas.drawLine(SIDE_PADDING.toFloat(), y, (RECEIPT_WIDTH - SIDE_PADDING).toFloat(), y, divider)
        y += 18f

        val rightCenter = RECEIPT_WIDTH - SIDE_PADDING - columnWidth / 2f
        val leftCenter = SIDE_PADDING + columnWidth / 2f
        canvas.drawText("الشق الأيمن", rightCenter, y, header)
        canvas.drawText("الشق الأيسر", leftCenter, y, header)
        y += 12f
        canvas.drawLine(SIDE_PADDING.toFloat(), y, (RECEIPT_WIDTH - SIDE_PADDING).toFloat(), y, divider)

        val middleX = RECEIPT_WIDTH / 2f
        val tableBottom = y + rowHeights.sum()
        canvas.drawLine(middleX, y, middleX, tableBottom, divider)
        y += 5f

        for (index in 0 until rows) {
            val rowHeight = rowHeights[index]
            drawItem(
                canvas, rightItems.getOrNull(index), middleX + COLUMN_GAP / 2f,
                columnWidth, nameWidth, false, y, paint(NAME_TEXT_SIZE, true, Paint.Align.RIGHT), qty
            )
            drawItem(
                canvas, leftItems.getOrNull(index), SIDE_PADDING.toFloat(),
                columnWidth, nameWidth, true, y, paint(NAME_TEXT_SIZE, true, Paint.Align.RIGHT), qty
            )
            y += rowHeight
            canvas.drawLine(SIDE_PADDING.toFloat(), y, (RECEIPT_WIDTH - SIDE_PADDING).toFloat(), y, rowDivider)
        }
        return bitmap
    }

    private fun wrappedHeight(text: String?, width: Int): Int {
        if (text.isNullOrBlank()) return 22
        val value = text.trim()
        val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = NAME_TEXT_SIZE
            typeface = Typeface.DEFAULT_BOLD
            color = Color.BLACK
        }
        return StaticLayout.Builder.obtain(value, 0, value.length, textPaint, width)
            .setAlignment(Layout.Alignment.ALIGN_OPPOSITE)
            .setIncludePad(false)
            .setLineSpacing(0f, 1f)
            .build()
            .height
            .coerceAtLeast(22)
    }

    private fun drawItem(
        canvas: Canvas,
        item: GroceryItemEntity?,
        columnLeft: Float,
        columnWidth: Int,
        nameWidth: Int,
        qtyOnLeft: Boolean,
        rowTop: Float,
        namePaint: Paint,
        qtyPaint: Paint
    ) {
        if (item == null) return
        val value = item.name.trim()
        val qtyX = if (qtyOnLeft) columnLeft + 20f else columnLeft + columnWidth - 20f
        val nameRight = if (qtyOnLeft) columnLeft + columnWidth - 5f else columnLeft + nameWidth + 5f
        val layout = StaticLayout.Builder.obtain(
            value, 0, value.length, TextPaint(namePaint), nameWidth
        ).setAlignment(Layout.Alignment.ALIGN_OPPOSITE)
            .setIncludePad(false)
            .setLineSpacing(0f, 1f)
            .build()

        canvas.save()
        canvas.translate(nameRight - nameWidth, rowTop)
        layout.draw(canvas)
        canvas.restore()
        canvas.drawText(item.qty.toString(), qtyX, rowTop + 14f, qtyPaint)
    }

    private fun paint(size: Float, bold: Boolean, align: Paint.Align): Paint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = size
            textAlign = align
            typeface = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        }

    fun bitmapToEscPosBytes(bitmap: Bitmap): ByteArray {
        val width = bitmap.width
        val height = bitmap.height
        val widthBytes = (width + 7) / 8
        val output = ByteArrayOutputStream()
        output.write(byteArrayOf(0x1B, 0x40))
        output.write(byteArrayOf(0x1B, 0x33, 0x00))
        val xL = (widthBytes and 0xFF).toByte()
        val xH = ((widthBytes shr 8) and 0xFF).toByte()
        val yL = (height and 0xFF).toByte()
        val yH = ((height shr 8) and 0xFF).toByte()
        output.write(byteArrayOf(0x1D, 0x76, 0x30, 0x00, xL, xH, yL, yH))
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
                        if (luminance < threshold) byteVal = byteVal or (1 shl (7 - b))
                    }
                }
                output.write(byteVal)
            }
        }
        output.write(byteArrayOf(0x1B, 0x64, 0x02))
        return output.toByteArray()
    }
}
