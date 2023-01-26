package info.skyblond.vazan

import android.content.Intent
import android.graphics.*
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.TextUnit
import androidx.core.graphics.get
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.zxing.common.BitMatrix
import info.skyblond.paperang.PaperangP2
import info.skyblond.vazan.ui.theme.Typography
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

/**
 * Render [String] to ALPHA_8 picture. Single line.
 * */
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

/**
 * Render [UUID] to picture, and then convert to bytes that [PaperangP2] can understand and print.
 * */
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

/**
 * Ensure the [text] is displayed in one single line.
 * If the [initialFontSize] is overflowed, then it will try 1% smaller,
 * until it fits. Super long text might result in super small font size.
 * */
@Composable
fun OneLineText(
    text: String, modifier: Modifier = Modifier,
    fontFamily: FontFamily = FontFamily.Default,
    initialFontSize: TextUnit = Typography.body1.fontSize
) {
    val readyToDraw = remember { mutableStateOf(false) }
    val textSize = remember { mutableStateOf(initialFontSize) }
    Text(
        text = text,
        modifier = modifier
            .width(IntrinsicSize.Max)
            .drawWithContent { if (readyToDraw.value) drawContent() },
        fontSize = textSize.value,
        fontFamily = fontFamily,
        softWrap = false, maxLines = 1,
        onTextLayout = { textLayoutResult ->
            if (textLayoutResult.didOverflowWidth) {
                // if overflow, try smaller size
                textSize.value = textSize.value * 0.99
            } else {
                readyToDraw.value = true
            }
        }
    )
}

/**
 * Text for [UUID], this will ensure the uuid will be displayed in a single line.
 * */
@Composable
fun UUIDText(uuid: UUID, modifier: Modifier = Modifier) =
    OneLineText(
        text = uuid.toString().uppercase(),
        modifier = modifier,
        fontFamily = FontFamily.Monospace,
    )