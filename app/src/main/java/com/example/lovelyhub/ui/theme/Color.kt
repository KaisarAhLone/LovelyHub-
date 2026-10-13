package com.example.lovelyhub.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Primary & Secondary Brand Colors
val LovelyPurpleDark = Color(0xFF4C1D95)
val LovelyPurplePrimary = Color(0xFF6D28D9)
val LovelyPurpleLight = Color(0xFF8B5CF6)
val LovelyPurpleSoft = Color(0xFFEDE9FE)
val LovelyAmber = Color(0xFFFFB703)
val LovelyYellow = Color(0xFFFBBF24)

// Neutral & Background Colors
val LovelyBgLight = Color(0xFFF4F6FB)
val LovelySurfaceWhite = Color(0xFFFFFFFF)
val LovelyTextDark = Color(0xFF1E1B4B)
val LovelyTextMuted = Color(0xFF64748B)
val LovelyBorder = Color(0xFFE2E8F0)

// Status & Badges
val LovelyRedBadge = Color(0xFFEF4444)
val LovelyGreenOnline = Color(0xFF22C55E)
val LovelyGoldRating = Color(0xFFF59E0B)

// Bottom Navigation Bar
val LovelyBottomNavBg = Color(0xFF13141F)
val LovelyNavActivePill = Color(0xFF6D28D9)
val LovelyNavInactive = Color(0xFF94A3B8)

// Gradients
val LovelyHeaderGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF3B0764),
        Color(0xFF581C87),
        Color(0xFF6D28D9),
        Color(0xFF7C3AED)
    )
)

val LovelyAppbarGradient = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFF581C87),
        Color(0xFF7C3AED)
    )
)

val LovelyCardGlassGradient = Brush.verticalGradient(
    colors = listOf(
        Color.White.copy(alpha = 0.95f),
        Color.White.copy(alpha = 0.90f)
    )
)

// Category Card Gradients
val CatRestaurantGradient = listOf(Color(0xFFFFF7ED), Color(0xFFFFEDD5))
val CatRestaurantColor = Color(0xFFFF7A00)

val CatRoomsGradient = listOf(Color(0xFFF0F9FF), Color(0xFFE0F2FE))
val CatRoomsColor = Color(0xFF0288D1)

val CatRentalsGradient = listOf(Color(0xFFFAF5FF), Color(0xFFF3E8FF))
val CatRentalsColor = Color(0xFF7C3AED)

val CatJobsGradient = listOf(Color(0xFFF0FDF4), Color(0xFFDCFCE7))
val CatJobsColor = Color(0xFF16A34A)

val CatMarketplaceGradient = listOf(Color(0xFFFDF2F8), Color(0xFFFCE7F3))
val CatMarketplaceColor = Color(0xFFDB2777)

val CatServicesGradient = listOf(Color(0xFFFEFCE8), Color(0xFFFEF9C3))
val CatServicesColor = Color(0xFFEAB308)

val CatFavoritesGradient = listOf(Color(0xFFFFF1F2), Color(0xFFFFE4E6))
val CatFavoritesColor = Color(0xFFE11D48)

// Legacy compatibility values
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)
val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)