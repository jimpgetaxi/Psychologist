package com.jimpgetaxi.psychologist.presentation.sessions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jimpgetaxi.psychologist.data.local.SessionEntity
import com.jimpgetaxi.psychologist.domain.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val repository: ChatRepository
) : ViewModel() {

    val sessions: StateFlow<List<SessionEntity>> = repository.getSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createSession(onCreated: (Long) -> Unit) {
        viewModelScope.launch {
            val id = repository.createNewSession()
            onCreated(id)
        }
    }

    fun deleteSession(sessionId: Long) {
        viewModelScope.launch {
            repository.deleteSession(sessionId)
        }
    }

    fun updateSessionTitle(sessionId: Long, newTitle: String) {
        viewModelScope.launch {
            repository.updateSessionTitle(sessionId, newTitle)
        }
    }
}
