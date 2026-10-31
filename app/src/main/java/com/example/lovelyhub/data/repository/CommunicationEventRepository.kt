package com.example.lovelyhub.data.repository

import com.example.lovelyhub.data.model.CommunicationEvent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineScope

class CommunicationEventRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun logEvent(
        channel: String, // "in_app", "whatsapp", "phone"
        action: String,  // "message_action", "call_action"
        status: String,  // "handoff_initiated", "call_initiated", "sent"
        recipientUserId: String = "",
        recipientDisplayName: String = "",
        listingId: String = "",
        listingTitle: String = "",
        listingType: String = ""
    ): Result<Unit> {
        return try {
            val currentUser = auth.currentUser
            val actorUid = currentUser?.uid ?: "anonymous_user"
            val actorName = currentUser?.displayName ?: "Campus Student"

            val docRef = firestore.collection("communicationEvents").document()
            val event = CommunicationEvent(
                id = docRef.id,
                actorUserId = actorUid,
                actorDisplayName = actorName,
                recipientUserId = recipientUserId,
                recipientDisplayName = recipientDisplayName,
                channel = channel,
                action = action,
                listingId = listingId,
                listingTitle = listingTitle,
                listingType = listingType,
                timestamp = System.currentTimeMillis(),
                status = status
            )

            docRef.set(event).await()
            Result.success(Unit)
        } catch (e: Exception) {
            // Never break user flow if logging fails
            Result.success(Unit)
        }
    }

    suspend fun getEvents(channel: String? = null, limit: Long = 100): List<CommunicationEvent> {
        return try {
            var query: Query = firestore.collection("communicationEvents")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(limit)

            if (!channel.isNullOrBlank()) {
                query = query.whereEqualTo("channel", channel)
            }

            val snapshot = query.get().await()
            snapshot.toObjects(CommunicationEvent::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun listenToEvents(
        channel: String? = null,
        limit: Long = 100,
        onUpdate: (List<CommunicationEvent>) -> Unit
    ): ListenerRegistration {
        var query: Query = firestore.collection("communicationEvents")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(limit)

        if (!channel.isNullOrBlank()) {
            query = query.whereEqualTo("channel", channel)
        }

        return query.addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                val list = snapshot.toObjects(CommunicationEvent::class.java)
                onUpdate(list)
            }
        }
    }

    companion object {
        private val repo = CommunicationEventRepository()

        fun recordWhatsAppMessageHandoff(
            listingId: String = "",
            listingTitle: String = "",
            listingType: String = "",
            recipientName: String = ""
        ) {
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                repo.logEvent(
                    channel = "whatsapp",
                    action = "message_action",
                    status = "handoff_initiated",
                    recipientDisplayName = recipientName,
                    listingId = listingId,
                    listingTitle = listingTitle,
                    listingType = listingType
                )
            }
        }

        fun recordPhoneCallInitiated(
            listingId: String = "",
            listingTitle: String = "",
            listingType: String = "",
            recipientName: String = ""
        ) {
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                repo.logEvent(
                    channel = "phone",
                    action = "call_action",
                    status = "call_initiated",
                    recipientDisplayName = recipientName,
                    listingId = listingId,
                    listingTitle = listingTitle,
                    listingType = listingType
                )
            }
        }

        fun recordWhatsAppCallInitiated(
            listingId: String = "",
            listingTitle: String = "",
            listingType: String = "",
            recipientName: String = ""
        ) {
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                repo.logEvent(
                    channel = "whatsapp",
                    action = "call_action",
                    status = "call_initiated",
                    recipientDisplayName = recipientName,
                    listingId = listingId,
                    listingTitle = listingTitle,
                    listingType = listingType
                )
            }
        }

        fun recordInAppMessage(
            recipientUserId: String = "",
            recipientDisplayName: String = "",
            listingId: String = "",
            listingTitle: String = "",
            listingType: String = ""
        ) {
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                repo.logEvent(
                    channel = "in_app",
                    action = "message_action",
                    status = "sent",
                    recipientUserId = recipientUserId,
                    recipientDisplayName = recipientDisplayName,
                    listingId = listingId,
                    listingTitle = listingTitle,
                    listingType = listingType
                )
            }
        }
    }
}
