package com.example.funny_cats

import android.app.Application
import android.os.StrictMode
import com.example.funny_cats.util.ThemeHelper
import com.squareup.leakcanary.core.BuildConfig
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
        }
    }

    private fun setupStrictMode() {
        // Детектируем проблемы в UI потоке
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectAll() // Детектим все проблемы
                .penaltyLog() // Логируем нарушения
                .penaltyFlashScreen() // Мигаем экраном при нарушениях (очень заметно!)
                .build()
        )

        // Детектируем проблемы с памятью
        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects() // Утечки БД
                .detectLeakedClosableObjects() // Утечки ресурсов
                .detectActivityLeaks() // Утечки Activity
                .detectLeakedRegistrationObjects() // Утечки BroadcastReceiver
                .penaltyLog() // Логируем нарушения
                .build()
        )
    }
}