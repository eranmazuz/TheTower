package com.example.thetower.ui

import android.content.res.Configuration
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import java.util.Locale

@Composable
fun LanguageWrapper(
    language: String,
    content: @Composable () -> Unit
) {
    val layoutDirection = if (language == "he") LayoutDirection.Rtl else LayoutDirection.Ltr

    val currentConfig = LocalConfiguration.current
    val currentContext = LocalContext.current
    val registryOwner = LocalActivityResultRegistryOwner.current ?: (currentContext as? ActivityResultRegistryOwner)

    // In Android, Hebrew is mapped to either "iw" or "he". We use "he".
    val locale = Locale(if (language == "he") "he" else "en")

    val localizedConfig = Configuration(currentConfig).apply {
        setLocale(locale)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
            setLayoutDirection(locale)
        }
    }

    val localizedContext = currentContext.createConfigurationContext(localizedConfig)

    if (registryOwner != null) {
        CompositionLocalProvider(
            LocalContext provides localizedContext,
            LocalLayoutDirection provides layoutDirection,
            LocalActivityResultRegistryOwner provides registryOwner
        ) {
            content()
        }
    } else {
        CompositionLocalProvider(
            LocalContext provides localizedContext,
            LocalLayoutDirection provides layoutDirection
        ) {
            content()
        }
    }
}
