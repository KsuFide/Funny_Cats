package com.example.funny_cats.di

import android.content.Context
import com.example.funny_cats.data.local.CatDatabase
import com.example.funny_cats.data.repository.CatBreedRepository
import com.example.funny_cats.data.repository.CatImageRepository
import com.example.funny_cats.data.repository.NotificationRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Singleton
    @Provides
    fun provideCatDatabase(@ApplicationContext context: Context): CatDatabase {
        return CatDatabase.getInstance(context)
    }

    @Singleton
    @Provides
    fun provideCatBreedRepository(database: CatDatabase): CatBreedRepository {
        return CatBreedRepository(database)
    }

    @Singleton
    @Provides
    fun provideCatImageRepository(database: CatDatabase): CatImageRepository {
        return CatImageRepository(database)
    }

    // УБИРАЕМ StatsRepository - он больше не нужен
    @Singleton
    @Provides
    fun provideNotificationRepository(
        database: CatDatabase,
        @ApplicationContext context: Context
    ): NotificationRepository {
        return NotificationRepository(database, context)
    }
}