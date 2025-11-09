package com.example.funny_cats.data.repository

import com.example.funny_cats.data.local.CatDatabase
import com.example.funny_cats.data.local.model.AppSettings
import com.example.funny_cats.data.local.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SettingsRepository @Inject constructor(
    private val database: CatDatabase
) {
    private val dao = database.appSettingsDao()

    fun getSettings(): Flow<AppSettings> {
        return dao.getSettings().map { settings ->
            settings ?: AppSettings().also {
                // Инициализируем настройки по умолчанию, если их нет
                initializeDefaultSettings()
            }
        }
    }

    suspend fun updateSettings(settings: AppSettings) {
        dao.updateSettings(settings)
    }

    suspend fun updateThemeMode(themeMode: ThemeMode) {
        dao.updateThemeMode(themeMode.name)
    }

    suspend fun updateRandomCatsSetting(enabled: Boolean) {
        dao.updateRandomCatsSetting(enabled)
    }

    suspend fun updateBreedImagesSetting(enabled: Boolean) {
        dao.updateBreedImagesSetting(enabled)
    }

    suspend fun updateComposeSetting(enabled: Boolean) {
        dao.updateComposeSetting(enabled)
    }

    suspend fun updateNotificationsEnabled(enabled: Boolean) {
        dao.updateNotificationsEnabled(enabled)
    }

    private suspend fun initializeDefaultSettings() {
        val defaultSettings = AppSettings()
        dao.insertSettings(defaultSettings)
    }
}