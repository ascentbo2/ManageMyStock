package com.managestock.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Gain = Color(0xFF1B9E4B)
val Loss = Color(0xFFD93025)
val Brand = Color(0xFF1565C0)

private val LightColors = lightColorScheme(
    primary = Brand,
    onPrimary = Color.White,
    background = Color(0xFFF5F6F8),
    surface = Color.White,
    onSurface = Color(0xFF1B1F24),
    surfaceVariant = Color(0xFFEDEFF2),
    onSurfaceVariant = Color(0xFF5F6670)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF64A0FF),
    onPrimary = Color(0xFF00205B),
    background = Color(0xFF0F1216),
    surface = Color(0xFF1A1E24),
    onSurface = Color(0xFFE6E8EB),
    surfaceVariant = Color(0xFF262B33),
    onSurfaceVariant = Color(0xFF9AA1AB)
)

@Composable
fun ManageStockTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content
    )
}
