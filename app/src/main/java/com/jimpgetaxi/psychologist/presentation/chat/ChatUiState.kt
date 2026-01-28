package com.jimpgetaxi.psychologist.presentation.chat

import com.jimpgetaxi.psychologist.data.local.MessageEntity

data class ChatUiState(
    val messages: List<MessageEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAiTyping: Boolean = false,
    val isCrisisDetected: Boolean = false,
    val streamingMessage: String? = null,
    val currentModel: String = "gemini-3-flash-preview",
    val availableModels: List<String> = emptyList()
)
