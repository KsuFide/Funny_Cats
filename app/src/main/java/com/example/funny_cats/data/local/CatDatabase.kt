package com.example.funny_cats.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.funny_cats.data.local.converters.ListConverters
import com.example.funny_cats.data.local.dao.CatBreedDao
import com.example.funny_cats.data.local.dao.CatImageDao
import com.example.funny_cats.data.local.dao.NotificationDao
import com.example.funny_cats.data.local.model.CatBreedEntity
import com.example.funny_cats.data.local.model.CatImage
import com.example.funny_cats.data.local.model.NotificationSetting

@Database(
    entities = [CatBreedEntity::class, CatImage::class, NotificationSetting::class],
    version = 4,
    exportSchema = false
)
abstract class CatDatabase : RoomDatabase() {

    abstract fun catBreedDao(): CatBreedDao
    abstract fun catImageDao(): CatImageDao
    abstract fun notificationDao(): NotificationDao // ДОБАВЛЯЕМ

    companion object {
        @Volatile
        private var INSTANCE: CatDatabase? = null

        fun getInstance(context: Context): CatDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CatDatabase::class.java,
                    "cat_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}