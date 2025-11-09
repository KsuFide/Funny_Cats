package com.example.funny_cats

import android.app.Application
import com.example.funny_cats.util.ThemeHelper
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CatFinderApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Применяем сохраненную тему при запуске приложения
        val savedTheme = ThemeHelper.getSavedTheme(this)
        ThemeHelper.applyTheme(savedTheme)
    }
}