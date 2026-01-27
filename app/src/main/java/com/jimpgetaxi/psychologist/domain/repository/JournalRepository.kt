package com.jimpgetaxi.psychologist.domain.repository

import com.jimpgetaxi.psychologist.domain.model.JournalEntry
import kotlinx.coroutines.flow.Flow

interface JournalRepository {
    fun getAllEntries(): Flow<List<JournalEntry>>
    suspend fun getEntryById(id: Long): JournalEntry?
    suspend fun saveEntry(entry: JournalEntry)
    suspend fun deleteEntry(entry: JournalEntry)
}
