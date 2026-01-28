package com.jimpgetaxi.psychologist.presentation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jimpgetaxi.psychologist.domain.repository.ChatRepository
import com.jimpgetaxi.psychologist.domain.usecase.DetectCrisisUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository: ChatRepository,
    private val detectCrisisUseCase: DetectCrisisUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var currentSessionId: Long = -1

    fun setSession(sessionId: Long) {
        currentSessionId = sessionId
        viewModelScope.launch {
            repository.getMessages(sessionId).collect { messages ->
                _uiState.update { it.copy(messages = messages) }
            }
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank() || currentSessionId == -1L) return

        // Check for crisis keywords locally
        if (detectCrisisUseCase(text)) {
            _uiState.update { it.copy(isCrisisDetected = true) }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isAiTyping = true, error = null) }
            
            repository.sendMessageStream(currentSessionId, text)
                .onStart { 
                    // No need to clear isAiTyping yet
                }
                .catch { e ->
                    val errorMessage = when {
                        e.message?.contains("429") == true -> "⚠️ Traffic Limit (429): Please wait a moment."
                        e.message?.contains("Quota") == true -> "⚠️ API Quota Exceeded."
                        e.message?.contains("503") == true -> "⚠️ Service Unavailable (503). Try again."
                        e.message?.contains("finishReason") == true -> "⚠️ Stopped by Safety Filters."
                        else -> "❌ Error: ${e.message ?: "Unknown error"}"
                    }
                    _uiState.update { it.copy(isAiTyping = false, error = errorMessage) }
                }
                .collect { fullText ->
                    _uiState.update { it.copy(streamingMessage = fullText) }
                }
            
            _uiState.update { it.copy(isAiTyping = false, streamingMessage = null) }
        }
    }
}
