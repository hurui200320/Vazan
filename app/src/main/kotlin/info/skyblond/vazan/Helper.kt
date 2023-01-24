package info.skyblond.vazan

import android.content.Intent
import android.graphics.*
import androidx.core.graphics.get
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.zxing.common.BitMatrix
import info.skyblond.paperang.PaperangP2
import java.util.*
import kotlin.experimental.or

fun BitMatrix.toByteArrays(): Array<ByteArray> {
    require(this.width <= PaperangP2.PRINT_BIT_PER_LINE) { "Too wide" }
    val widthLeftMargin = (PaperangP2.PRINT_BIT_PER_LINE - this.width) / 2
    val widthRightMargin = PaperangP2.PRINT_BIT_PER_LINE - this.width - widthLeftMargin
    return (0 until this.height).map { h ->
        val bits = (0 until this.width).map { this[it, h] }
        (BooleanArray(widthLeftMargin).toList() + bits + BooleanArray(widthRightMargin).toList())
            .chunked(8) {
                var b: Byte = 0
                for (i in it.indices) if (it[i]) b = (b or (1 shl (7 - i)).toByte())
                b
            }.toByteArray()
    }.toTypedArray()
}

fun Intent.getBarcodes(): List<Pair<Int, ByteArray>> {
    val size = this.getLongExtra("size", 0)
    return (0 until size).map {
        this.getIntExtra(
            "format$it",
            Barcode.FORMAT_UNKNOWN
        ) to this.getByteArrayExtra("data$it")
    }.filter { it.first != Barcode.FORMAT_UNKNOWN && it.second != null }
        .map { it.first to it.second!! }
}

private fun textAsBitmap(text: String): Bitmap {
    val paint = Paint().apply {
        textSize = 27f // max size we can get with bold monospace
        color = Color.BLACK
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        textAlign = Paint.Align.LEFT
    }
    val baseline: Float = -paint.ascent() // ascent() is negative
    val width = (paint.measureText(text) + 0.5f).toInt() // round
    val height = (baseline + paint.descent() + 0.5f).toInt()
    val image = Bitmap.createBitmap(width, height, Bitmap.Config.ALPHA_8)
    val canvas = Canvas(image)
    canvas.drawText(text, 0f, baseline, paint)
    return image
}

fun UUID.toPrintableByteArrays(): Array<ByteArray> {
    val bitmap = textAsBitmap(this.toString().uppercase())
    require(bitmap.width <= PaperangP2.PRINT_BIT_PER_LINE) { "Too wide" }
    val widthLeftMargin = (PaperangP2.PRINT_BIT_PER_LINE - bitmap.width) / 2
    val widthRightMargin = PaperangP2.PRINT_BIT_PER_LINE - bitmap.width - widthLeftMargin
    return (0 until bitmap.height).map { h ->
        val bits = (0 until bitmap.width).map { bitmap[it, h] != 0 }
        (BooleanArray(widthLeftMargin).toList() + bits + BooleanArray(widthRightMargin).toList())
            .chunked(8) {
                var b: Byte = 0
                for (i in it.indices) if (it[i]) b = (b or (1 shl (7 - i)).toByte())
                b
            }.toByteArray()
    }.toTypedArray()
}