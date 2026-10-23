package com.example.lovelyhub.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AppColorScheme = lightColorScheme(
    primary = Color(0xFF3F51B5),
    onPrimary = Color.White,
    secondary = Color(0xFFFFB300),
    onSecondary = Color(0xFF1E1E2D),
    background = Color(0xFFF3F4F9),
    onBackground = Color(0xFF1E1E2D),
    surface = Color.White,
    onSurface = Color(0xFF1E1E2D),
    surfaceVariant = Color(0xFFE8EAF6),
    onSurfaceVariant = Color(0xFF1E1E2D)
)

@Composable
fun LovelyHubTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = Typography,
        content = content
    )
}
