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
private val LightColorScheme = lightColorScheme(
    primary = SpendraBlack,
    onPrimary = SpendraWhite,
    primaryContainer = SpendraBlack,
    onPrimaryContainer = SpendraWhite,
    secondary = SpendraOrange,
    onSecondary = SpendraBlack,
    tertiary = SpendraGreen,
    background = SpendraCream,
    onBackground = SpendraBlack,
    surface = SpendraWhite,
    onSurface = SpendraBlack,
    outline = SpendraBlack,
    error = SpendraRed
)

@Composable
fun SpendraTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}