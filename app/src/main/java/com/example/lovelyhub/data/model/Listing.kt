package com.example.lovelyhub.data.model

data class Listing(
    val id: String = "",
    val category: String = "",
    val title: String = "",
    val price: String = "",
    val phone: String = "",
    val location: String = "",
    val imageUrl: String = "",
    val status: String = "Open",
    val ownerUid: String = "",
    val isFavorite: Boolean = false,
    val menuItems: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)
