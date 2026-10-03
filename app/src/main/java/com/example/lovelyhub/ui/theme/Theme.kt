package com.example.lovelyhub.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF9D4EDD),
    onPrimary = Color.White,
    secondary = Color(0xFF7B2CBF),
    onSecondary = Color.White,
    background = Color(0xFF121218),
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF1E1E28),
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF2D2D3A),
    onSurfaceVariant = Color(0xFFCAC4D0)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF7926E1),
    onPrimary = Color.White,
    secondary = Color(0xFF511CB2),
    onSecondary = Color.White,
    background = Color(0xFFF6F7FB),
    onBackground = Color(0xFF1C1B1F),
    surface = Color.White,
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFF0E5FC),
    onSurfaceVariant = Color(0xFF49454F)
)

@Composable
fun LovelyHubTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
