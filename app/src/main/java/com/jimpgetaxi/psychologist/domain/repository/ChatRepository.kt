package com.jimpgetaxi.psychologist.domain.repository

import com.jimpgetaxi.psychologist.data.local.MessageEntity
import com.jimpgetaxi.psychologist.data.local.SessionEntity
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun getMessages(sessionId: Long): Flow<List<MessageEntity>>
    suspend fun sendMessage(sessionId: Long, text: String): Result<Unit>
    fun sendMessageStream(sessionId: Long, text: String): Flow<String>
    fun getSessions(): Flow<List<SessionEntity>>
    suspend fun createNewSession(): Long
    suspend fun deleteSession(sessionId: Long)
    suspend fun updateSessionTitle(sessionId: Long, newTitle: String)
}
