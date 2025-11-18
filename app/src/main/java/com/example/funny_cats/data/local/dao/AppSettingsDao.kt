package com.example.funny_cats.data.local.dao

import androidx.room.*
import com.example.funny_cats.data.local.model.AppSettings
import kotlinx.coroutines.flow.Flow

@Dao
interface AppSettingsDao {

    @Query("SELECT * FROM app_settings WHERE id = :id")
    fun getSettings(id: String = AppSettings.DEFAULT_SETTINGS_ID): Flow<AppSettings?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: AppSettings)

    @Update
    suspend fun updateSettings(settings: AppSettings)

    @Query("UPDATE app_settings SET themeMode = :themeMode WHERE id = :id")
    suspend fun updateThemeMode(themeMode: String, id: String = AppSettings.DEFAULT_SETTINGS_ID)

    @Query("UPDATE app_settings SET showRandomCats = :enabled WHERE id = :id")
    suspend fun updateRandomCatsSetting(enabled: Boolean, id: String = AppSettings.DEFAULT_SETTINGS_ID)

    @Query("UPDATE app_settings SET showBreedImages = :enabled WHERE id = :id")
    suspend fun updateBreedImagesSetting(enabled: Boolean, id: String = AppSettings.DEFAULT_SETTINGS_ID)

    @Query("UPDATE app_settings SET useCompose = :enabled WHERE id = :id")
    suspend fun updateComposeSetting(enabled: Boolean, id: String = AppSettings.DEFAULT_SETTINGS_ID)

    @Query("UPDATE app_settings SET notificationsEnabled = :enabled WHERE id = :id")
    suspend fun updateNotificationsEnabled(enabled: Boolean, id: String = AppSettings.DEFAULT_SETTINGS_ID)
}