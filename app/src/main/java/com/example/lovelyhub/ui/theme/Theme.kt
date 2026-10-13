package com.example.lovelyhub.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = LovelyPurpleLight,
    onPrimary = Color.White,
    secondary = LovelyAmber,
    onSecondary = LovelyTextDark,
    background = Color(0xFF0F172A),
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF1E293B),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFFCBD5E1)
)

private val LightColorScheme = lightColorScheme(
    primary = LovelyPurplePrimary,
    onPrimary = Color.White,
    secondary = LovelyAmber,
    onSecondary = LovelyTextDark,
    background = LovelyBgLight,
    onBackground = LovelyTextDark,
    surface = LovelySurfaceWhite,
    onSurface = LovelyTextDark,
    surfaceVariant = LovelyPurpleSoft,
    onSurfaceVariant = LovelyTextMuted
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
