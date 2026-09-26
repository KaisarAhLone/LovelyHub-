package com.example.lovelyhub.data.model

data class Listing(
    val id: String = "",
    val category: String = "",
    val title: String = "",
    val price: String = "",
    val phone: String = "",
    val imageUrl: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
