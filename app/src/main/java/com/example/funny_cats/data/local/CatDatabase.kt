package com.example.funny_cats.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(
    entities = [CatBreedEntity::class, CatImage::class, NotificationSetting::class, AppSettings::class],
    version = 6, // Увеличиваем версию для миграции
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
                    .fallbackToDestructiveMigration()
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Создаем индексы для ускорения запросов
                            db.execSQL("CREATE INDEX index_cat_images_favorites ON cat_images(isInFavorites)")
                            db.execSQL("CREATE INDEX index_cat_images_last_updated ON cat_images(lastUpdated)")
                            db.execSQL("CREATE INDEX index_cat_breeds_name ON cat_breeds(name)")
                            db.execSQL("CREATE INDEX index_cat_breeds_favorites ON cat_breeds(isInFavorites)")
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        // Метод для тестирования
        fun getTestInstance(context: Context): CatDatabase {
            return Room.inMemoryDatabaseBuilder(
                context.applicationContext,
                CatDatabase::class.java
            ).build()
        }
    }
}