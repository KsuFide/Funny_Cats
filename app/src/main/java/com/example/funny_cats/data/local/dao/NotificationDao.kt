package com.example.funny_cats.data.local.dao

import androidx.room.*
import com.example.funny_cats.data.local.model.NotificationSetting
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {

    @Query("SELECT * FROM notification_settings")
    fun getAllSettings(): Flow<List<NotificationSetting>>

    @Query("SELECT * FROM notification_settings WHERE id = :settingId")
    suspend fun getSettingById(settingId: String): NotificationSetting?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetting(setting: NotificationSetting)

    @Update
    suspend fun updateSetting(setting: NotificationSetting)

    @Query("UPDATE notification_settings SET isEnabled = :isEnabled WHERE id = :settingId")
    suspend fun updateEnabledStatus(settingId: String, isEnabled: Boolean)

    @Query("DELETE FROM notification_settings")
    suspend fun clearAll()
}