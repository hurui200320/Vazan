package info.skyblond.vazan.ui.composable

import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.TextUnit

/**
 * Ensure the [text] is displayed in one single line.
 * If the [initialFontSize] is overflowed, then it will try 1% smaller,
 * until it fits. Super long text might result in super small font size.
 * */
@Composable
fun OneLineText(
    text: String, modifier: Modifier = Modifier,
    fontFamily: FontFamily? = null,
    initialFontSize: TextUnit = TextUnit.Unspecified
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