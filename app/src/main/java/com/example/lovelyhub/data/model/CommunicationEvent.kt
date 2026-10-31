package com.example.lovelyhub.data.model

data class CommunicationEvent(
    val id: String = "",
    val actorUserId: String = "",
    val actorDisplayName: String = "",
    val recipientUserId: String = "",
    val recipientDisplayName: String = "",
    val channel: String = "", // "in_app", "whatsapp", "phone"
    val action: String = "",  // "message_action", "call_action"
    val listingId: String = "",
    val listingTitle: String = "",
    val listingType: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "initiated" // "handoff_initiated", "call_initiated", "sent"
)
