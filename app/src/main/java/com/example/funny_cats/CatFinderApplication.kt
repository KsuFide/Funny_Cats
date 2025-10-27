package com.example.funny_cats

import android.app.Application
import dagger.hilt.android.HiltAndroidApp


@HiltAndroidApp
class CatFinderApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Hilt автоматически сгенерирует весь необходимый код
    }
}