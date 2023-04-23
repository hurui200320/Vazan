package info.skyblond.vazan.domain

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Typeface
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.common.BitMatrix
import com.google.zxing.datamatrix.DataMatrixWriter
import com.google.zxing.datamatrix.encoder.SymbolShapeHint
import com.google.zxing.oned.Code128Writer
import kotlin.math.roundToInt

@Suppress("SameParameterValue")
object PrintUtils {
    private fun dataMatrix(width: Int, height: Int, content: String): BitMatrix =
        DataMatrixWriter().encode(
            content, BarcodeFormat.DATA_MATRIX, width, height,
            // force square
            mapOf(EncodeHintType.DATA_MATRIX_SHAPE to SymbolShapeHint.FORCE_SQUARE)
        )

    private fun code128(width: Int, height: Int, content: String): BitMatrix =
        Code128Writer().encode(content, BarcodeFormat.CODE_128, width, height)

    private fun BitMatrix.toBitMap(): Bitmap {
        val image = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888)
        for (y in 0 until this.height) {
            for (x in 0 until this.width) {
                if (this[x, y])
                    image.setPixel(x, y, Color.BLACK)
                else
                    image.setPixel(x, y, Color.WHITE)
            }
        }
        return image
    }

    private fun textToBitmap(text: String, fontSize: Double): Bitmap {
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

    private fun Bitmap.rotate(angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)
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

    /**
     * Generate a [width] by [height] image with [barcode] and [text].
     * The [barcode] will be placed at horizontal center of the image, spacing [yOffset] between the top.
     * The [text] will be placed at horizontal center of the image, spacing [ySpacing] between the [barcode].
     *
     * The whole picture will be offset horizontally by [xOffset].
     * */
    private fun generateImage(
        width: Int, height: Int, barcode: Bitmap, text: Bitmap,
        xOffset: Float, yOffset: Float, ySpacing: Float
    ): Bitmap {
        val image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(image)
        // white background
        canvas.drawRect(0f, 0f, image.width.toFloat(), image.height.toFloat(),
            Paint().apply { color = Color.WHITE })
        // barcode
        canvas.drawBitmap(barcode, (image.width - barcode.width) / 2.0f + xOffset, yOffset, null)
        // text
        canvas.drawBitmap(
            text,
            (image.width - text.width) / 2.0f + xOffset,
            yOffset + barcode.height + ySpacing,
            null
        )
        return image
    }

    fun generate80By60(str: String): Bitmap {
        val barcode = dataMatrix(320, 320, str).toBitMap()
        val text = textToBitmap(str, 64.0)
        val image = generateImage(480, 640, barcode, text, 0f, 50f, 40f)
        // now we get 640 by 480 -> 80 by 60 under 203 dpi
        return image.rotate(-90f)
    }

    fun generate60By80(str: String): Bitmap {
        val barcode = dataMatrix(320, 320, str).toBitMap()
        val text = textToBitmap(str, 64.0)
        // the printer can't print the left side, need count that on xOffset
        return generateImage(480, 640, barcode, text, -12f, 67f, 40f)
    }

    fun generate70By30(str: String): Bitmap {
        val barcode = code128(500, 150, str).toBitMap()
        val text = textToBitmap(str, 54.0)
        return generateImage(560, 240, barcode, text, -12f, 15f, -3f)
    }
}