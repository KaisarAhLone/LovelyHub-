package com.example.lovelyhub.data.model

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "Student",
    val businessName: String = "",
    val businessPhone: String = "",
    val businessWhatsApp: String = "",
    val businessLocation: String = "",
    val openTime: String = "09:00 AM",
    val closeTime: String = "10:00 PM",
    val is24Hours: Boolean = false,
    val photoUrl: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

enum class UserRole(val displayName: String, val defaultCategory: String) {
    STUDENT("Student", "Marketplace"),
    RESTAURANT_OWNER("Restaurant Owner", "Restaurants"),
    ROOM_OWNER("Room / PG Owner", "Rooms"),
    RENTAL_PROVIDER("Rental Provider", "Rentals"),
    SERVICE_PROVIDER("Service Provider", "Services")
}
