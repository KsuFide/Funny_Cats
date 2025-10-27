package com.example.funny_cats.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.funny_cats.data.local.dao.CatBreedDao
import com.example.funny_cats.data.local.dao.CatImageDao
import com.example.funny_cats.data.local.model.CatBreedEntity
import com.example.funny_cats.data.local.model.CatImage


@Database(
    entities = [CatBreedEntity::class, CatImage::class],
    version = 2,
    exportSchema = false
)
abstract class CatDatabase : RoomDatabase() {

    abstract fun catBreedDao(): CatBreedDao
    abstract fun catImageDao(): CatImageDao

    companion object {
        @Volatile
        private var INSTANCE: CatDatabase? = null

        fun getInstance(context: Context): CatDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CatDatabase::class.java,
                    "cat_database"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}