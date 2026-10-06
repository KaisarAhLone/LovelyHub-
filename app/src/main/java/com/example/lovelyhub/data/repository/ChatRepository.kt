package com.example.lovelyhub.data.repository

import com.example.lovelyhub.data.model.ChatConversation
import com.example.lovelyhub.data.model.ChatMessage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ChatRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    private val repositoryScope = CoroutineScope(Dispatchers.IO)

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
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val messages = snapshot.toObjects(ChatMessage::class.java)
                    trySend(messages)
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
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    if (snapshot.isEmpty) {
                        trySend(emptyList())
                        return@addSnapshotListener
                    }

                    repositoryScope.launch {
                        val conversations = mutableListOf<ChatConversation>()
                        for (doc in snapshot.documents) {
                            val participants = doc.get("participants") as? List<*> ?: emptyList<String>()
                            val otherUid = participants.firstOrNull { it != currentUid }?.toString() ?: ""
                            val lastMsg = doc.getString("lastMessage") ?: ""
                            val time = doc.getLong("timestamp") ?: System.currentTimeMillis()
                            val chatId = doc.id

                            if (otherUid.isNotBlank()) {
                                try {
                                    val userDoc = firestore.collection("users").document(otherUid).get().await()
                                    val name = userDoc.getString("businessName").takeIf { !it.isNullOrBlank() }
                                        ?: userDoc.getString("name")
                                        ?: "Campus User"
                                    val photo = userDoc.getString("photoUrl") ?: ""

                                    conversations.add(
                                        ChatConversation(
                                            chatId = chatId,
                                            otherUid = otherUid,
                                            otherName = name,
                                            otherPhotoUrl = photo,
                                            lastMessage = lastMsg,
                                            timestamp = time
                                        )
                                    )
                                } catch (_: Exception) {}
                            }
                        }
                        conversations.sortByDescending { it.timestamp }
                        trySend(conversations.toList())
                    }
                }
            }

        awaitClose { listener.remove() }
    }
}
