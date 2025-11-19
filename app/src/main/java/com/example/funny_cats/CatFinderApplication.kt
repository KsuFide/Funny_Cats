package com.example.funny_cats

import android.app.Application
import android.os.StrictMode
import com.example.funny_cats.util.ThemeHelper
import com.example.funny_cats.util.Logger
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CatFinderApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Применяем сохраненную тему при запуске приложения
        val savedTheme = ThemeHelper.getSavedTheme(this)
        ThemeHelper.applyTheme(savedTheme)

        // Включаем StrictMode только в debug сборке
        if (BuildConfig.DEBUG) {
            setupStrictMode()
            Logger.d("Application started in DEBUG mode")
        } else {
            Logger.d("Application started in RELEASE mode")
        }
    }

    private fun setupStrictMode() {
        // Детектируем проблемы в UI потоке
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .penaltyFlashScreen()
                .build()
        )

        // Детектируем проблемы с памятью
        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .detectActivityLeaks()
                .detectLeakedRegistrationObjects()
                .penaltyLog()
                .build()
        )
    }
}