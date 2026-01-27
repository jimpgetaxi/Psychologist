package com.jimpgetaxi.psychologist.data.repository

import com.jimpgetaxi.psychologist.data.local.JournalDao
import com.jimpgetaxi.psychologist.data.local.JournalEntity
import com.jimpgetaxi.psychologist.domain.model.JournalEntry
import com.jimpgetaxi.psychologist.domain.repository.JournalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class JournalRepositoryImpl @Inject constructor(
    private val journalDao: JournalDao
) : JournalRepository {

    override fun getAllEntries(): Flow<List<JournalEntry>> {
        return journalDao.getAllEntries().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getEntryById(id: Long): JournalEntry? {
        return journalDao.getEntryById(id)?.toDomain()
    }

    override suspend fun saveEntry(entry: JournalEntry) {
        journalDao.insertEntry(entry.toEntity())
    }

    override suspend fun deleteEntry(entry: JournalEntry) {
        journalDao.deleteEntry(entry.toEntity())
    }

    private fun JournalEntity.toDomain() = JournalEntry(
        id = id,
        title = title,
        content = content,
        timestamp = timestamp
    )

    private fun JournalEntry.toEntity() = JournalEntity(
        id = id,
        title = title,
        content = content,
        timestamp = timestamp
    )
}
