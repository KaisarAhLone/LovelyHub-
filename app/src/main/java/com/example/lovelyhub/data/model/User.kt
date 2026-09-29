package com.example.lovelyhub.data.model

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "Student",
    val businessName: String = "",
    val businessPhone: String = "",
    val businessLocation: String = "",
    val photoUrl: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

enum class UserRole(val displayName: String) {
    STUDENT("Student"),
    BUSINESS_PROVIDER("Shopkeeper / Room Owner / Rental Provider")
}
