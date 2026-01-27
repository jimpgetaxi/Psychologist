package com.jimpgetaxi.psychologist.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalDao {
    @Query("SELECT * FROM journal_entries ORDER BY timestamp DESC")
    fun getAllEntries(): Flow<List<JournalEntity>>

    @Query("SELECT * FROM journal_entries WHERE id = :id")
    suspend fun getEntryById(id: Long): JournalEntity?

    @Query("SELECT * FROM journal_entries ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentEntries(limit: Int): List<JournalEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: JournalEntity): Long

    @Delete
    suspend fun deleteEntry(entry: JournalEntity)

    @Update
    suspend fun updateEntry(entry: JournalEntity)
}
