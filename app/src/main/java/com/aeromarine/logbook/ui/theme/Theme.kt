package com.aeromarine.logbook.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorPalette = darkColorScheme(
    primary = CosmicColors.NeonPink,
    secondary = CosmicColors.ElectricPurple,
    background = CosmicColors.SpaceStart,
    surface = CosmicColors.SpaceMiddle,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = CosmicColors.LightGray
)

@Composable
fun BubbleOrbitTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorPalette,
        typography = Typography,
        content = content
    )
}

val EggYellow = Color(0xFF36DEF4)
val CoralRed = Color(0xFFFF6B6B)
val SkyBlue = Color(0xFF4A90E2)
val GrassGreen = Color(0xFF3DD598)
val CreamWhite = Color(0xFFFFF9E6)

private val LightColorScheme = lightColorScheme(
    primary = EggYellow,
    onPrimary = Color.Black,
    secondary = SkyBlue,
    onSecondary = Color.White,
    tertiary = GrassGreen,
    background = CreamWhite,
    surface = Color.White,
    onSurface = Color.Black,
    surfaceVariant = Color(0xFFF0F0F0),
    onSurfaceVariant = Color(0xFF666666),
    error = CoralRed
)

@Composable
fun ChickenTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> LightColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
