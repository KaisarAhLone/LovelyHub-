package com.example.lovelyhub.data.model

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "Student",
    val photoUrl: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

enum class UserRole(val displayName: String) {
    STUDENT("Student"),
    ROOM_OWNER("Room Owner / Rental Provider"),
    SERVICE_PROVIDER("Service Provider"),
    SHOPKEEPER("Shopkeeper / Employer")
}
