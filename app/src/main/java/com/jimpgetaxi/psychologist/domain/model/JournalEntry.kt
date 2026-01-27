package com.jimpgetaxi.psychologist.domain.model

data class JournalEntry(
    val id: Long = 0,
    val title: String,
    val content: String,
    val timestamp: Long
)
