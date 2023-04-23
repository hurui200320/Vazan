package info.skyblond.vazan.domain

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Typeface
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.datamatrix.DataMatrixWriter
import com.google.zxing.datamatrix.encoder.SymbolShapeHint
import kotlin.math.roundToInt

object PrintUtils {
    fun generateDataMatrix(width: Int, height: Int, content: String): Bitmap {
        val writer = DataMatrixWriter()
        val matrix = writer.encode(
            content, BarcodeFormat.DATA_MATRIX, width, height,
            // force square
            mapOf(EncodeHintType.DATA_MATRIX_SHAPE to SymbolShapeHint.FORCE_SQUARE)
        )
        val image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        for (y in 0 until matrix.height) {
            for (x in 0 until matrix.width) {
                if (matrix[x, y])
                    image.setPixel(x, y, Color.BLACK)
                else
                    image.setPixel(x, y, Color.WHITE)
            }
        }

        return image
    }

    fun textAsBitmap(text: String, fontSize: Double): Bitmap {
        val paint = Paint().apply {
            textSize = fontSize.toFloat()
            color = Color.BLACK
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            textAlign = Paint.Align.LEFT
        }
        val baseline: Float = -paint.ascent() // ascent() is negative
        val width = (paint.measureText(text) + 0.5f).toInt() // round
        val height = (baseline + paint.descent() + 0.5f).toInt()
        val image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(image)
        canvas.drawText(text, 0f, baseline, paint)
        return image
    }

    fun Bitmap.rotate(angle: Double): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle.toFloat())
        return Bitmap.createBitmap(
            this, 0, 0, width, height, matrix, true
        )
    }

    private fun Bitmap.getGrayPixel(x: Int, y: Int): Int {
        val c = this.getPixel(x, y)
        val r = (c shr 16) and 0xFF
        val g = (c shr 8) and 0xFF
        val b = c and 0xFF
        return (0.3 * r + 0.59 * g + 0.11 * b).roundToInt()
    }

    fun Bitmap.toDataArray(): Array<IntArray> =
        (0 until this.height).map { y ->
            (0 until this.width).map { x ->
                this.getGrayPixel(x, y)
            }.toIntArray()
        }.toTypedArray()

    fun Array<BooleanArray>.collapse(): ByteArray {
        val width = (this[0].size / 8.0).roundToInt()
        val result = ByteArray(width * this.size)
        for (y in this.indices) {
            val row = this[y]
            for (x in row.indices) {
                val wordIndex = x / 8
                val bitIndex = 7 - (x % 8)
                var w = result[wordIndex + y * width].toInt()
                val mask = 1 shl bitIndex
                w = if (row[x]) { // set the bit to 0 for black
                    w and mask.inv()
                } else {
                    w or mask
                }
                result[wordIndex + y * width] = w.toByte()
            }
        }
        return result
    }

    fun generate60By80(str: String): Bitmap {
        // 480 in 203 dpi is 60mm, 640 in 203 dpi is 80mm
        val image = Bitmap.createBitmap(480, 640, Bitmap.Config.ARGB_8888)
        val yOffset = 50.0f
        val ySpacing = 40.0f
        val barcode = generateDataMatrix(320, 320, str)
        val text = textAsBitmap(str, 64.0)

        val canvas = Canvas(image)
        // white background
        canvas.drawRect(0f, 0f, image.width.toFloat(), image.height.toFloat(),
            Paint().apply { color = Color.WHITE })
        // barcode
        canvas.drawBitmap(barcode, (image.width - barcode.width) / 2.0f, yOffset, null)
        // text
        canvas.drawBitmap(
            text, (image.width - text.width) / 2.0f, yOffset + barcode.height + ySpacing, null
        )

        return image
    }
}