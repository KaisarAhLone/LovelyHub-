package com.example.lovelyhub.ui.chats

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lovelyhub.data.model.ChatConversation
import com.example.lovelyhub.data.model.ChatMessage
import com.example.lovelyhub.data.repository.ChatRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel(
    private val repository: ChatRepository = ChatRepository()
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _conversations = MutableStateFlow<List<ChatConversation>>(emptyList())
    val conversations: StateFlow<List<ChatConversation>> = _conversations.asStateFlow()

    private val _isLoadingConversations = MutableStateFlow(false)
    val isLoadingConversations: StateFlow<Boolean> = _isLoadingConversations.asStateFlow()

    private val _isSendingMessage = MutableStateFlow(false)
    val isSendingMessage: StateFlow<Boolean> = _isSendingMessage.asStateFlow()

    private var conversationJob: Job? = null
    private var messageJob: Job? = null

    fun initLocalCache(context: Context) {
        repository.initLocalCache(context)
        val cached = repository.getCachedConversations()
        if (cached.isNotEmpty()) {
            _conversations.value = cached
            _isLoadingConversations.value = false
        }
    }

    fun clearOnSignOut() {
        conversationJob?.cancel()
        messageJob?.cancel()
        _conversations.value = emptyList()
        _messages.value = emptyList()
        repository.clearOnSignOut()
    }

    fun listenToMessages(context: Context, receiverUid: String) {
        repository.initLocalCache(context)
        repository.markChatAsRead(receiverUid)
        messageJob?.cancel()

        val cachedMsg = repository.getCachedMessages(receiverUid)
        if (cachedMsg.isNotEmpty()) {
            _messages.value = cachedMsg
        }

        messageJob = viewModelScope.launch {
            repository.getRealtimeMessages(receiverUid).collect { msgList ->
                _messages.value = msgList
            }
        }
    }

    fun listenToConversations(context: Context) {
        repository.initLocalCache(context)
        val cached = repository.getCachedConversations()
        if (cached.isNotEmpty()) {
            _conversations.value = cached
            _isLoadingConversations.value = false
        } else if (_conversations.value.isEmpty()) {
            _isLoadingConversations.value = true
        }

        if (conversationJob?.isActive == true) return

        conversationJob = viewModelScope.launch {
            val timeoutJob = launch {
                delay(1500L)
                _isLoadingConversations.value = false
            }

            repository.getRealtimeConversations().collect { convList ->
                timeoutJob.cancel()
                _conversations.value = convList
                _isLoadingConversations.value = false
            }
        }
    }

    fun sendMessage(receiverUid: String, text: String, onComplete: () -> Unit = {}) {
        if (text.isBlank()) return
        _isSendingMessage.value = true
        viewModelScope.launch {
            repository.sendMessage(receiverUid, text)
            _isSendingMessage.value = false
            onComplete()
        }
    }
}
