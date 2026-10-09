package com.example.lovelyhub.ui.chats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lovelyhub.data.model.ChatConversation
import com.example.lovelyhub.data.model.ChatMessage
import com.example.lovelyhub.data.repository.ChatRepository
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

    private val _isLoadingConversations = MutableStateFlow(true)
    val isLoadingConversations: StateFlow<Boolean> = _isLoadingConversations.asStateFlow()

    fun listenToMessages(receiverUid: String) {
        viewModelScope.launch {
            repository.getRealtimeMessages(receiverUid).collect { msgList ->
                _messages.value = msgList
            }
        }
    }

    fun listenToConversations() {
        viewModelScope.launch {
            _isLoadingConversations.value = true
            repository.getRealtimeConversations().collect { convList ->
                _conversations.value = convList
                _isLoadingConversations.value = false
            }
        }
    }

    fun sendMessage(receiverUid: String, text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            repository.sendMessage(receiverUid, text)
        }
    }
}
