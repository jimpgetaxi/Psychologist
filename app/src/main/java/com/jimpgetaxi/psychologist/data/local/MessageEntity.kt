package com.jimpgetaxi.psychologist.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.jimpgetaxi.psychologist.domain.model.Sender

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val content: String,
    val timestamp: Long,
    val sender: Sender,
    val sentimentScore: Float? = null
)
