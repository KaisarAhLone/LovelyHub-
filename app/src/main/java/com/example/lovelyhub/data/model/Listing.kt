package com.example.lovelyhub.data.model

data class Listing(
    val id: String = "",
    val category: String = "",
    val title: String = "",
    val price: String = "",
    val phone: String = "",
    val whatsApp: String = "",
    val location: String = "",
    val imageUrl: String = "",
    val status: String = "Open",
    val rating: String = "4.3",
    val openTime: String = "09:00 AM",
    val closeTime: String = "10:00 PM",
    val is24Hours: Boolean = false,
    val ownerUid: String = "",
    val isFavorite: Boolean = false,
    val menuItems: List<String> = emptyList(),
    val isShop: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
