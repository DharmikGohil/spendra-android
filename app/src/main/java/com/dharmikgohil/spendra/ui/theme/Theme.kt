package com.dharmikgohil.spendra.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Define the Spendra Color Scheme (Light Theme Only as per spec)
private val SpendraColorScheme = lightColorScheme(
    primary = SpendraOrange,
    onPrimary = SpendraCream,
    secondary = SpendraYellow,
    onSecondary = SpendraBlack,
    tertiary = SpendraGreen,
    background = SpendraCream,
    onBackground = SpendraBlack,
    surface = SpendraCream,
    onSurface = SpendraBlack,
    error = SpendraRed,
    onError = SpendraCream,
    outline = SpendraGray
)

@Composable
fun SpendraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    // We disable it to enforce the Spendra Brand Palette
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = SpendraColorScheme
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}