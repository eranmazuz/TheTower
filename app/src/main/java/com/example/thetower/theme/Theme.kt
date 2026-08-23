package com.example.thetower.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SystemColorScheme = darkColorScheme(
    primary = SystemCyan,
    onPrimary = Color(0xFF0C101D),
    primaryContainer = SystemSurfaceHighlight,
    onPrimaryContainer = SystemCyan,
    secondary = SystemIndigo,
    onSecondary = Color.White,
    secondaryContainer = SystemSurfaceHighlight,
    onSecondaryContainer = SystemTextPrimary,
    tertiary = SystemGold,
    onTertiary = Color.Black,
    background = SystemVoid,
    onBackground = SystemTextPrimary,
    surface = SystemSurface,
    onSurface = SystemTextPrimary,
    surfaceVariant = SystemSurfaceElevated,
    onSurfaceVariant = SystemTextSecondary,
    outline = SystemBorder,
    error = SystemRuby,
    onError = Color.White
)

@Composable
fun TheTowerTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = SystemColorScheme,
        typography = Typography,
        content = content
    )
}
