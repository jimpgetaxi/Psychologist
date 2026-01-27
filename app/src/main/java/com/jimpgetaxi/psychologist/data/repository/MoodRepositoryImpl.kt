package com.jimpgetaxi.psychologist.data.repository

import com.jimpgetaxi.psychologist.data.local.MoodDao
import com.jimpgetaxi.psychologist.data.local.MoodEntity
import com.jimpgetaxi.psychologist.domain.repository.MoodRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MoodRepositoryImpl @Inject constructor(
    private val moodDao: MoodDao
) : MoodRepository {
    override fun getAllMoods(): Flow<List<MoodEntity>> = moodDao.getAllMoods()

    override suspend fun saveMood(moodValue: Int, note: String?) {
        val mood = MoodEntity(
            timestamp = System.currentTimeMillis(),
            moodValue = moodValue,
            note = note
        )
        moodDao.insertMood(mood)
    }

    override suspend fun getRecentMoods(limit: Int): List<MoodEntity> {
        return moodDao.getRecentMoods(limit)
    }
}
