package com.example.lovelyhub.data.model

data class ChatMessage(
    val id: String = "",
    val senderUid: String = "",
    val receiverUid: String = "",
    val messageText: String = "",
    val read: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

data class ChatConversation(
    val chatId: String = "",
    val otherUid: String = "",
    val otherName: String = "",
    val otherPhotoUrl: String = "",
    val otherPhone: String = "",
    val otherWhatsApp: String = "",
    val lastMessage: String = "",
    val unreadCount: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)
