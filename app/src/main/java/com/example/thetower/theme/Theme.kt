package com.example.thetower.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val RpgColorScheme = darkColorScheme(
    primary = RpgGold,
    onPrimary = Color(0xFF13121D),
    primaryContainer = RpgSlotSurface,
    onPrimaryContainer = RpgGold,
    secondary = RpgCyan,
    onSecondary = Color.White,
    secondaryContainer = RpgSlotSurface,
    onSecondaryContainer = RpgTextPrimary,
    tertiary = RpgEmerald,
    onTertiary = Color.Black,
    background = RpgBackground,
    onBackground = RpgTextPrimary,
    surface = RpgCardSurface,
    onSurface = RpgTextPrimary,
    surfaceVariant = RpgSlotSurface,
    onSurfaceVariant = RpgTextSecondary,
    outline = RpgBorder,
    error = RpgRuby,
    onError = Color.White
)

@Composable
fun TheTowerTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = RpgColorScheme,
        typography = Typography,
        content = content
    )
}
