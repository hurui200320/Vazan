package info.skyblond.vazan.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable

private val DarkColorPalette = darkColors(
        primary = MaterialColor.Blue700,
        primaryVariant = MaterialColor.Blue900,
        secondary = MaterialColor.Teal700
)

private val LightColorPalette = lightColors(
    primary = MaterialColor.Cyan500,
    primaryVariant = MaterialColor.Cyan700,
    secondary = MaterialColor.LightGreen200
)

@Composable
fun VazanTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
            colors = colors,
            typography = Typography,
            shapes = Shapes,
            content = content
    )
}