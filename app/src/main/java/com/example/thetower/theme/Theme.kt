package com.example.thetower.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val RetroColorScheme = darkColorScheme(
    primary = RetroAmber,
    onPrimary = Color.Black,
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
    onError = Color.Black
)

@Composable
fun TheTowerTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = RetroColorScheme,
        typography = Typography,
        content = content
    )
}
