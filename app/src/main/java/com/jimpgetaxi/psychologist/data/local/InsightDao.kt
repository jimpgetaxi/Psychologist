package com.jimpgetaxi.psychologist.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface InsightDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInsight(insight: InsightEntity)

    @Query("SELECT * FROM insights ORDER BY timestamp DESC LIMIT 20")
    fun getAllInsights(): Flow<List<InsightEntity>>

    @Query("SELECT * FROM insights ORDER BY timestamp DESC LIMIT 20")
    suspend fun getRecentInsights(): List<InsightEntity>
    
    @Query("DELETE FROM insights")
    suspend fun deleteAllInsights()
}
