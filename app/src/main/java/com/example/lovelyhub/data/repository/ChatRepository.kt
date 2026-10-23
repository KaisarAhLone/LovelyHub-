package com.example.lovelyhub.data.repository

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
import java.util.concurrent.ConcurrentHashMap

class ChatRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    private val repositoryScope = CoroutineScope(Dispatchers.IO)
    private val userCache = ConcurrentHashMap<String, CachedUserInfo>()

    private data class CachedUserInfo(
        val name: String,
        val photoUrl: String,
        val phone: String,
        val whatsApp: String
    )

    fun generateChatId(uid1: String, uid2: String): String {
        return if (uid1 < uid2) "${uid1}_$uid2" else "${uid2}_$uid1"
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

        return try {
            msgRef.set(chatMsg).await()

            val chatMeta = mapOf(
                "chatId" to chatId,
                "participants" to listOf(senderUid, receiverUid),
                "lastMessage" to messageText.trim(),
                "timestamp" to System.currentTimeMillis()
            )

            firestore.collection("chats").document(chatId).set(chatMeta).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getRealtimeMessages(receiverUid: String): Flow<List<ChatMessage>> = callbackFlow {
        val senderUid = auth.currentUser?.uid ?: ""
        if (senderUid.isBlank() || receiverUid.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val chatId = generateChatId(senderUid, receiverUid)
        val listener = firestore.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val messages = snapshot.toObjects(ChatMessage::class.java)
                trySend(messages)

                repositoryScope.launch {
                    for (doc in snapshot.documents) {
                        val msgReceiver = doc.getString("receiverUid") ?: ""
                        val isRead = doc.getBoolean("read") ?: false
                        if (msgReceiver == senderUid && !isRead) {
                            try {
                                doc.reference.update("read", true).await()
                            } catch (_: Exception) {}
                        }
                    }
                }
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

        val listener = firestore.collection("chats")
            .whereArrayContains("participants", currentUid)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || snapshot.isEmpty) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                repositoryScope.launch {
                    val deferredList = snapshot.documents.map { doc ->
                        async(Dispatchers.IO) {
                            val participants = doc.get("participants") as? List<*> ?: emptyList<Any>()
                            val otherUid = participants.firstOrNull { it.toString() != currentUid }?.toString() ?: ""
                            val lastMsg = doc.getString("lastMessage") ?: ""
                            val time = doc.getLong("timestamp") ?: System.currentTimeMillis()
                            val chatId = doc.id

                            if (otherUid.isBlank()) return@async null

                            val cached = userCache[otherUid]
                            val userInfo = if (cached != null) {
                                cached
                            } else {
                                try {
                                    val userDoc = firestore.collection("users").document(otherUid).get().await()
                                    val name = userDoc.getString("businessName").takeIf { !it.isNullOrBlank() }
                                        ?: userDoc.getString("name")
                                        ?: "Campus User"
                                    val photo = userDoc.getString("photoUrl") ?: ""
                                    val phone = userDoc.getString("businessPhone").takeIf { !it.isNullOrBlank() } ?: ""
                                    val whatsApp = userDoc.getString("businessWhatsApp").takeIf { !it.isNullOrBlank() } ?: phone

                                    val fetched = CachedUserInfo(name, photo, phone, whatsApp)
                                    userCache[otherUid] = fetched
                                    fetched
                                } catch (_: Exception) {
                                    CachedUserInfo("Campus User", "", "", "")
                                }
                            }

                            val unreadCount = try {
                                val unreadDocs = firestore.collection("chats")
                                    .document(chatId)
                                    .collection("messages")
                                    .whereEqualTo("receiverUid", currentUid)
                                    .whereEqualTo("read", false)
                                    .get()
                                    .await()
                                unreadDocs.size()
                            } catch (_: Exception) {
                                0
                            }

                            ChatConversation(
                                chatId = chatId,
                                otherUid = otherUid,
                                otherName = userInfo.name,
                                otherPhotoUrl = userInfo.photoUrl,
                                otherPhone = userInfo.phone,
                                otherWhatsApp = userInfo.whatsApp,
                                lastMessage = lastMsg,
                                unreadCount = unreadCount,
                                timestamp = time
                            )
                        }
                    }

                    val results = deferredList.awaitAll().filterNotNull().sortedByDescending { it.timestamp }
                    trySend(results)
                }
            }

        awaitClose { listener.remove() }
    }
}
