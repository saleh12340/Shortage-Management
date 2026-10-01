package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = SplitRightBlue,
    onPrimary = Color.White,
    primaryContainer = SplitRightBg,
    onPrimaryContainer = SplitRightBlueLight,
    secondary = SplitLeftGreen,
    onSecondary = Color.White,
    secondaryContainer = SplitLeftBg,
    onSecondaryContainer = SplitLeftGreenLight,
    tertiary = PosAmber,
    onTertiary = Color.Black,
    tertiaryContainer = PosAmberBg,
    onTertiaryContainer = PosAmberLight,
    background = PosDarkBackground,
    onBackground = PosTextPrimary,
    surface = PosSurfaceDark,
    onSurface = PosTextPrimary,
    surfaceVariant = PosCardDark,
    onSurfaceVariant = PosTextSecondary,
    outline = PosCardBorder,
    error = PosRedActive,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // POS High Contrast Dark Theme as default
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
