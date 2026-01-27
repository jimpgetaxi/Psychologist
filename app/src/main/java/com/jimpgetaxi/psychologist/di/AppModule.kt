package com.jimpgetaxi.psychologist.di

import android.content.Context
import android.util.Log
import androidx.room.Room
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.generationConfig
import com.jimpgetaxi.psychologist.BuildConfig
import com.jimpgetaxi.psychologist.data.local.JournalDao
import com.jimpgetaxi.psychologist.data.local.MessageDao
import com.jimpgetaxi.psychologist.data.local.PsychologistDatabase
import com.jimpgetaxi.psychologist.domain.model.SystemPrompts
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PsychologistDatabase {
        return Room.databaseBuilder(
            context,
            PsychologistDatabase::class.java,
            "psychologist_db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideMessageDao(db: PsychologistDatabase): MessageDao = db.messageDao()

    @Provides
    fun provideMoodDao(db: PsychologistDatabase) = db.moodDao()

    @Provides
    fun provideJournalDao(db: PsychologistDatabase): JournalDao = db.journalDao()

    @Provides
    fun provideInsightDao(db: PsychologistDatabase) = db.insightDao()
}