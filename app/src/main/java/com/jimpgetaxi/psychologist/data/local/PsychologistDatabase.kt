package com.jimpgetaxi.psychologist.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [MessageEntity::class, SessionEntity::class, MoodEntity::class, JournalEntity::class, InsightEntity::class], version = 4, exportSchema = false)
@TypeConverters(Converters::class)
abstract class PsychologistDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
    abstract fun moodDao(): MoodDao
    abstract fun journalDao(): JournalDao
    abstract fun insightDao(): InsightDao
}
