package com.example.lovelyhub.data.model

data class AdminAuditLog(
    val id: String = "",
    val adminUid: String = "",
    val adminEmail: String = "",
    val action: String = "", // "LISTING_APPROVED", "LISTING_REJECTED", "LISTING_DISABLED", "LISTING_RESTORED"
    val targetListingId: String = "",
    val targetListingTitle: String = "",
    val targetCategory: String = "",
    val details: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
