package com.example.lovelyhub.data.repository

import android.content.Context
import com.example.lovelyhub.data.local.LocalChatCache
import com.example.lovelyhub.data.model.ChatConversation
import com.example.lovelyhub.data.model.ChatMessage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.ConcurrentHashMap

class ChatRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    private val repositoryScope = CoroutineScope(Dispatchers.IO)
    private val userCache = ConcurrentHashMap<String, CachedUserInfo>()
    private var localCache: LocalChatCache? = null

    fun initLocalCache(context: Context) {
        if (localCache == null) {
            localCache = LocalChatCache(context.applicationContext)
        }
    }

    fun clearOnSignOut() {
        userCache.clear()
        localCache?.clearMemoryCache()
    }

    private data class CachedUserInfo(
        val name: String,
        val photoUrl: String,
        val phone: String,
        val whatsApp: String
    )

    fun generateChatId(uid1: String, uid2: String): String {
        return if (uid1 < uid2) "${uid1}_$uid2" else "${uid2}_$uid1"
    }

    private fun generateMessagePreview(messageText: String): String {
        return if (messageText.contains("NEW CAMPUS ORDER")) {
            "🍔 Campus Order Placed"
        } else if (messageText.contains("data:image")) {
            "📷 Image Attached"
        } else {
            messageText.trim().take(80)
        }
    }

    suspend fun sendMessage(receiverUid: String, messageText: String): Result<Unit> {
        val senderUid = auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))
        if (messageText.isBlank()) return Result.success(Unit)

        val chatId = generateChatId(senderUid, receiverUid)
        val msgRef = firestore.collection("chats").document(chatId).collection("messages").document()

        val chatMsg = ChatMessage(
            id = msgRef.id,
            senderUid = senderUid,
            receiverUid = receiverUid,
            messageText = messageText.trim(),
            read = false,
            timestamp = System.currentTimeMillis()
        )

        val preview = generateMessagePreview(messageText)

        return try {
            msgRef.set(chatMsg).await()

            val chatMeta = mapOf(
                "chatId" to chatId,
                "participants" to listOf(senderUid, receiverUid),
                "lastMessage" to preview,
                "lastSenderUid" to senderUid,
                "isRead" to false,
                "timestamp" to System.currentTimeMillis()
            )

            firestore.collection("chats").document(chatId).set(chatMeta).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getCachedConversations(): List<ChatConversation> {
        return localCache?.getCachedConversations() ?: emptyList()
    }

    fun getCachedMessages(receiverUid: String): List<ChatMessage> {
        val senderUid = auth.currentUser?.uid ?: ""
        if (senderUid.isBlank() || receiverUid.isBlank()) return emptyList()
        val chatId = generateChatId(senderUid, receiverUid)
        return localCache?.getCachedMessages(chatId) ?: emptyList()
    }

    fun getRealtimeMessages(receiverUid: String): Flow<List<ChatMessage>> = callbackFlow {
        val senderUid = auth.currentUser?.uid ?: ""
        if (senderUid.isBlank() || receiverUid.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val chatId = generateChatId(senderUid, receiverUid)
        val cached = localCache?.getCachedMessages(chatId) ?: emptyList()
        if (cached.isNotEmpty()) {
            trySend(cached.filter { it.senderUid == senderUid || it.receiverUid == senderUid })
        }

        val listener = firestore.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .limitToLast(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    return@addSnapshotListener
                }

                val messages = snapshot.toObjects(ChatMessage::class.java)
                    .filter { it.senderUid == senderUid || it.receiverUid == senderUid }
                localCache?.saveMessages(chatId, messages)
                trySend(messages)
            }

        awaitClose { listener.remove() }
    }

    fun getRealtimeConversations(): Flow<List<ChatConversation>> = callbackFlow {
        val currentUid = auth.currentUser?.uid ?: ""
        if (currentUid.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val cached = localCache?.getCachedConversations() ?: emptyList()
        if (cached.isNotEmpty()) {
            trySend(cached.filter { it.chatId.contains(currentUid) })
        }

        val listener = firestore.collection("chats")
            .whereArrayContains("participants", currentUid)
            .limit(30)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || snapshot.isEmpty) {
                    if (cached.isEmpty()) {
                        trySend(emptyList())
                    }
                    return@addSnapshotListener
                }

                repositoryScope.launch {
                    val deferredList = snapshot.documents.map { doc ->
                        async(Dispatchers.IO) {
                            val participants = doc.get("participants") as? List<*> ?: emptyList<Any>()
                            if (!participants.contains(currentUid)) return@async null

                            val otherUid = participants.firstOrNull { it.toString() != currentUid }?.toString() ?: ""
                            val lastMsg = doc.getString("lastMessage") ?: ""
                            val time = doc.getLong("timestamp") ?: System.currentTimeMillis()
                            val chatId = doc.id

                            if (otherUid.isBlank()) return@async null

                            val cachedUser = userCache[otherUid]
                            val userInfo = if (cachedUser != null) {
                                cachedUser
                            } else {
                                val fetched = withTimeoutOrNull(2000L) {
                                    try {
                                        val userDoc = firestore.collection("users").document(otherUid).get().await()
                                        val name = userDoc.getString("businessName").takeIf { !it.isNullOrBlank() }
                                            ?: userDoc.getString("name")
                                            ?: "Campus User"
                                        val photo = userDoc.getString("photoUrl") ?: ""
                                        val phone = userDoc.getString("businessPhone").takeIf { !it.isNullOrBlank() } ?: ""
                                        val whatsApp = userDoc.getString("businessWhatsApp").takeIf { !it.isNullOrBlank() } ?: phone
                                        CachedUserInfo(name, photo, phone, whatsApp)
                                    } catch (_: Exception) {
                                        null
                                    }
                                } ?: CachedUserInfo("Campus User", "", "", "")

                                userCache[otherUid] = fetched
                                fetched
                            }

                            val lastSenderUid = doc.getString("lastSenderUid") ?: ""
                            val isRead = doc.getBoolean("isRead") ?: true
                            val unread = if (lastSenderUid.isNotBlank() && lastSenderUid != currentUid && !isRead) 1 else 0

                            ChatConversation(
                                chatId = chatId,
                                otherUid = otherUid,
                                otherName = userInfo.name,
                                otherPhotoUrl = userInfo.photoUrl,
                                otherPhone = userInfo.phone,
                                otherWhatsApp = userInfo.whatsApp,
                                lastMessage = generateMessagePreview(lastMsg),
                                unreadCount = unread,
                                timestamp = time
                            )
                        }
                    }

                    val results = deferredList.awaitAll().filterNotNull().sortedByDescending { it.timestamp }
                    localCache?.saveConversations(results)
                    trySend(results)
                }
            }

        awaitClose { listener.remove() }
    }

    fun markChatAsRead(receiverUid: String) {
        val senderUid = auth.currentUser?.uid ?: return
        if (receiverUid.isBlank()) return
        val chatId = generateChatId(senderUid, receiverUid)
        try {
            firestore.collection("chats").document(chatId).update("isRead", true)
        } catch (_: Exception) {}
    }
}
