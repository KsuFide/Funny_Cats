package com.example.funny_cats.util

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

object ThemeHelper {

    fun applyTheme(themeMode: String) {
        when (themeMode) {
            "LIGHT" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "DARK" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    fun saveThemePreference(context: Context, themeMode: String) {
        context.getSharedPreferences("app_theme", Context.MODE_PRIVATE)
            .edit()
            .putString("theme_mode", themeMode)
            .apply()
    }

    fun getSavedTheme(context: Context): String {
        return context.getSharedPreferences("app_theme", Context.MODE_PRIVATE)
            .getString("theme_mode", "SYSTEM") ?: "SYSTEM"
    }
}