package com.jimpgetaxi.psychologist.di

import com.jimpgetaxi.psychologist.data.repository.ChatRepositoryImpl
import com.jimpgetaxi.psychologist.data.repository.JournalRepositoryImpl
import com.jimpgetaxi.psychologist.data.repository.MoodRepositoryImpl
import com.jimpgetaxi.psychologist.domain.repository.ChatRepository
import com.jimpgetaxi.psychologist.domain.repository.JournalRepository
import com.jimpgetaxi.psychologist.domain.repository.MoodRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindChatRepository(
        chatRepositoryImpl: ChatRepositoryImpl
    ): ChatRepository

    @Binds
    @Singleton
    abstract fun bindMoodRepository(
        moodRepositoryImpl: MoodRepositoryImpl
    ): MoodRepository

    @Binds
    @Singleton
    abstract fun bindJournalRepository(
        journalRepositoryImpl: JournalRepositoryImpl
    ): JournalRepository
}
