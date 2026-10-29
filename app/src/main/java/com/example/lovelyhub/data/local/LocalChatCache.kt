package com.example.lovelyhub.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.lovelyhub.data.model.ChatConversation
import com.example.lovelyhub.data.model.ChatMessage
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap

class LocalChatCache(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("lovelyhub_chat_cache", Context.MODE_PRIVATE)
    private val memoryConversations = ConcurrentHashMap<String, ChatConversation>()
    private val memoryMessages = ConcurrentHashMap<String, List<ChatMessage>>()
    private val ioScope = CoroutineScope(Dispatchers.IO)

    init {
        loadConversationsFromPrefs()
    }

    private fun getCurrentUid(): String {
        return FirebaseAuth.getInstance().currentUser?.uid ?: ""
    }

    fun clearMemoryCache() {
        memoryConversations.clear()
        memoryMessages.clear()
    }

    private fun sanitizeText(raw: String): String {
        if (raw.isBlank()) return ""
        return if (raw.contains("data:image") || raw.contains("[IMAGE:")) {
            if (raw.contains("NEW CAMPUS ORDER")) {
                "🍔 Campus Order Placed"
            } else {
                "📷 Image Attached"
            }
        } else {
            raw.take(120).trim()
        }
    }

    private fun loadConversationsFromPrefs() {
        val uid = getCurrentUid()
        if (uid.isBlank()) return

        val jsonStr = try {
            prefs.getString("cached_conversations_$uid", null)
        } catch (_: Exception) {
            null
        } ?: return

        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val conv = ChatConversation(
                    chatId = obj.optString("chatId"),
                    otherUid = obj.optString("otherUid"),
                    otherName = obj.optString("otherName").ifBlank { "Campus User" },
                    otherPhotoUrl = obj.optString("otherPhotoUrl"),
                    otherPhone = obj.optString("otherPhone"),
                    otherWhatsApp = obj.optString("otherWhatsApp"),
                    lastMessage = sanitizeText(obj.optString("lastMessage")),
                    unreadCount = obj.optInt("unreadCount"),
                    timestamp = obj.optLong("timestamp")
                )
                if (conv.chatId.isNotBlank() && conv.chatId.contains(uid)) {
                    memoryConversations[conv.chatId] = conv
                }
            }
        } catch (_: Exception) {
            prefs.edit().remove("cached_conversations_$uid").apply()
        }
    }

    fun saveConversations(conversations: List<ChatConversation>) {
        val uid = getCurrentUid()
        if (uid.isBlank()) return

        memoryConversations.clear()
        for (conv in conversations) {
            if (conv.chatId.contains(uid)) {
                val sanitized = conv.copy(
                    lastMessage = sanitizeText(conv.lastMessage),
                    otherPhotoUrl = if (conv.otherPhotoUrl.startsWith("data:image")) "" else conv.otherPhotoUrl
                )
                memoryConversations[sanitized.chatId] = sanitized
            }
        }

        ioScope.launch {
            val array = JSONArray()
            for (conv in memoryConversations.values) {
                val obj = JSONObject().apply {
                    put("chatId", conv.chatId)
                    put("otherUid", conv.otherUid)
                    put("otherName", conv.otherName)
                    put("otherPhotoUrl", if (conv.otherPhotoUrl.startsWith("data:image")) "" else conv.otherPhotoUrl)
                    put("otherPhone", conv.otherPhone)
                    put("otherWhatsApp", conv.otherWhatsApp)
                    put("lastMessage", sanitizeText(conv.lastMessage))
                    put("unreadCount", conv.unreadCount)
                    put("timestamp", conv.timestamp)
                }
                array.put(obj)
            }
            try {
                prefs.edit().putString("cached_conversations_$uid", array.toString()).apply()
            } catch (_: Exception) {}
        }
    }

    fun getCachedConversations(): List<ChatConversation> {
        val uid = getCurrentUid()
        if (uid.isBlank()) return emptyList()

        if (memoryConversations.isEmpty()) {
            loadConversationsFromPrefs()
        }

        return memoryConversations.values
            .filter { it.chatId.contains(uid) }
            .sortedByDescending { it.timestamp }
    }

    fun saveMessages(chatId: String, messages: List<ChatMessage>) {
        val uid = getCurrentUid()
        if (uid.isBlank() || !chatId.contains(uid)) return

        val userFilteredMsgs = messages.filter { it.senderUid == uid || it.receiverUid == uid }
        memoryMessages[chatId] = userFilteredMsgs

        ioScope.launch {
            val array = JSONArray()
            val toSave = userFilteredMsgs.takeLast(40)
            for (msg in toSave) {
                val cleanMsgText = if (msg.messageText.length > 800 && !msg.messageText.contains("NEW CAMPUS ORDER")) {
                    msg.messageText.take(200)
                } else if (msg.messageText.contains("data:image/jpeg;base64,")) {
                    msg.messageText.replace(Regex("data:image/jpeg;base64,[A-Za-z0-9+/=]+"), "[IMAGE_DATA]")
                } else {
                    msg.messageText
                }

                val obj = JSONObject().apply {
                    put("id", msg.id)
                    put("senderUid", msg.senderUid)
                    put("receiverUid", msg.receiverUid)
                    put("messageText", cleanMsgText)
                    put("read", msg.read)
                    put("timestamp", msg.timestamp)
                }
                array.put(obj)
            }
            try {
                prefs.edit().putString("messages_${uid}_$chatId", array.toString()).apply()
            } catch (_: Exception) {}
        }
    }

    fun getCachedMessages(chatId: String): List<ChatMessage> {
        val uid = getCurrentUid()
        if (uid.isBlank() || !chatId.contains(uid)) return emptyList()

        val inMemory = memoryMessages[chatId]
        if (inMemory != null && inMemory.isNotEmpty()) {
            return inMemory.filter { it.senderUid == uid || it.receiverUid == uid }
        }

        val jsonStr = try {
            prefs.getString("messages_${uid}_$chatId", null)
        } catch (_: Exception) {
            null
        } ?: return emptyList()

        val result = mutableListOf<ChatMessage>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val msg = ChatMessage(
                    id = obj.optString("id"),
                    senderUid = obj.optString("senderUid"),
                    receiverUid = obj.optString("receiverUid"),
                    messageText = obj.optString("messageText"),
                    read = obj.optBoolean("read"),
                    timestamp = obj.optLong("timestamp")
                )
                if (msg.senderUid == uid || msg.receiverUid == uid) {
                    result.add(msg)
                }
            }
            memoryMessages[chatId] = result
        } catch (_: Exception) {}
        return result
    }
}
