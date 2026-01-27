package com.jimpgetaxi.psychologist.domain.repository

import com.jimpgetaxi.psychologist.data.local.MoodEntity
import kotlinx.coroutines.flow.Flow

interface MoodRepository {
    fun getAllMoods(): Flow<List<MoodEntity>>
    suspend fun saveMood(moodValue: Int, note: String? = null)
    suspend fun getRecentMoods(limit: Int): List<MoodEntity>
}
