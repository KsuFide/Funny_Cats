package com.example.funny_cats.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_settings")
data class AppSettings(
    @PrimaryKey
    val id: String = "main_settings",

    // Настройки контента
    val showRandomCats: Boolean = true,
    val showBreedImages: Boolean = true,
    val showFavoriteBreeds: Boolean = false,
    val contentLimit: Int = 20,

    // Настройки темы
    val themeMode: ThemeMode = ThemeMode.SYSTEM,

    // Настройки уведомлений
    val notificationsEnabled: Boolean = true,
    val dailyReminders: Boolean = false,
    val weeklySummary: Boolean = false,

    // Экспериментальные функции
    val useCompose: Boolean = false
) {
    companion object {
        const val DEFAULT_SETTINGS_ID = "main_settings"
    }
}

enum class ThemeMode {
    LIGHT, DARK, SYSTEM
}