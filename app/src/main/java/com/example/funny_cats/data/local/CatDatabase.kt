package com.example.funny_cats.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.funny_cats.data.local.converters.ListConverters
import com.example.funny_cats.data.local.dao.AppSettingsDao
import com.example.funny_cats.data.local.dao.CatBreedDao
import com.example.funny_cats.data.local.dao.CatImageDao
import com.example.funny_cats.data.local.dao.NotificationDao
import com.example.funny_cats.data.local.model.AppSettings
import com.example.funny_cats.data.local.model.CatBreedEntity
import com.example.funny_cats.data.local.model.CatImage
import com.example.funny_cats.data.local.model.NotificationSetting

@Database(
    entities = [CatBreedEntity::class, CatImage::class, NotificationSetting::class, AppSettings::class],
    version = 6,
    exportSchema = false
)
@TypeConverters(ListConverters::class)
abstract class CatDatabase : RoomDatabase() {

    abstract fun catBreedDao(): CatBreedDao
    abstract fun catImageDao(): CatImageDao
    abstract fun notificationDao(): NotificationDao
    abstract fun appSettingsDao(): AppSettingsDao

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
                    .addMigrations(MIGRATION_5_6) // ИСПОЛЬЗУЕМ МИГРАЦИЮ ВМЕСТО УДАЛЕНИЯ
                    .build()
                INSTANCE = instance
                instance
            }
        }

        // Миграция с версии 5 на 6 - добавляем поле lastViewed
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Добавляем новое поле lastViewed в таблицу cat_breeds
                database.execSQL("ALTER TABLE cat_breeds ADD COLUMN lastViewed INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}