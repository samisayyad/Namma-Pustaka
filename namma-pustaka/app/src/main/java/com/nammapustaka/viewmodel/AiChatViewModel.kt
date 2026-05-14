package com.nammapustaka.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nammapustaka.data.model.ChatMessage
import com.nammapustaka.data.repository.AiRepository
import com.nammapustaka.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AiChatViewModel @Inject constructor(
    private val aiRepo: AiRepository
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(listOf(
        ChatMessage(
            id = UUID.randomUUID().toString(),
            content = "Hello! I'm Pustaka, your AI library assistant. Ask me about books, get summaries, or discover what to read next!",
            isFromUser = false
        )
    ))
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping.asStateFlow()

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        val userMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            content = text,
            isFromUser = true
        )
        _messages.value = _messages.value + userMsg
        _isTyping.value = true

        val history = _messages.value.dropLast(1).map { msg ->
            Pair(msg.content, msg.isFromUser)
        }

        viewModelScope.launch {
            aiRepo.chat(text, history).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _isTyping.value = false
                        val aiMsg = ChatMessage(
                            id = UUID.randomUUID().toString(),
                            content = result.data,
                            isFromUser = false
                        )
                        _messages.value = _messages.value + aiMsg
                    }
                    is Resource.Error -> {
                        _isTyping.value = false
                        val errorMsg = ChatMessage(
                            id = UUID.randomUUID().toString(),
                            content = "Sorry, I couldn't respond right now. Please try again.",
                            isFromUser = false
                        )
                        _messages.value = _messages.value + errorMsg
                    }
                    is Resource.Loading -> {}
                }
            }
        }
    }

    fun clearChat() {
        _messages.value = listOf(
            ChatMessage(
                id = UUID.randomUUID().toString(),
                content = "Hello! I'm Pustaka, your AI library assistant. Ask me about books, get summaries, or discover what to read next!",
                isFromUser = false
            )
        )
    }
}
