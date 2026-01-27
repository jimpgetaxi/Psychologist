package com.jimpgetaxi.psychologist.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MoodDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMood(mood: MoodEntity)

    @Query("SELECT * FROM moods ORDER BY timestamp DESC")
    fun getAllMoods(): Flow<List<MoodEntity>>

    @Query("SELECT * FROM moods ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentMoods(limit: Int): List<MoodEntity>
}
