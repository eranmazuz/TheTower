package com.example.thetower.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val RetroColorScheme = lightColorScheme(
    primary = RetroAmber,
    onPrimary = Color.White,
    secondary = RetroGreen,
    onSecondary = Color.White,
    tertiary = RetroRed,
    background = RetroBlack,
    onBackground = RetroLightGrey,
    surface = RetroDarkGrey,
    onSurface = RetroLightGrey,
    surfaceVariant = RetroCardGrey,
    onSurfaceVariant = RetroMutedGrey,
    outline = RetroAmber,
    error = RetroRed,
    onError = Color.White
)

@Composable
fun TheTowerTheme(
    darkTheme: Boolean = false, // Use bright light theme
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = RetroColorScheme,
        typography = Typography,
        content = content
    )
}
