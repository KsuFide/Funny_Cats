package com.example.funny_cats.data.repository

import android.content.Context
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.funny_cats.data.local.CatDatabase
import com.example.funny_cats.data.local.model.NotificationSetting
import com.example.funny_cats.worker.CatNotificationWorker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class NotificationRepository @Inject constructor(
    private val database: CatDatabase,
    private val context: Context
) {

    private val dao = database.notificationDao()

    fun getAllSettings(): Flow<List<NotificationSetting>> {
        return dao.getAllSettings()
    }

    suspend fun getSettingById(settingId: String): NotificationSetting? {
        return dao.getSettingById(settingId)
    }

    suspend fun updateSetting(setting: NotificationSetting) {
        dao.updateSetting(setting)
    }

    suspend fun updateEnabledStatus(settingId: String, isEnabled: Boolean) {
        dao.updateEnabledStatus(settingId, isEnabled)
    }

    // Инициализация начальных настроек
    suspend fun initializeDefaultSettings() {
        val dailySetting = dao.getSettingById(NotificationSetting.DAILY_REMINDER_ID)

        if (dailySetting == null) {
            val defaultSettings = listOf(
                NotificationSetting(
                    id = NotificationSetting.DAILY_REMINDER_ID,
                    title = "Ежедневное напоминание",
                    description = "Напоминание посмотреть котиков каждый день в 20:00",
                    isEnabled = false,
                    hour = 20,
                    minute = 0
                ),
                NotificationSetting(
                    id = NotificationSetting.INACTIVITY_REMINDER_ID,
                    title = "Напоминание о неактивности",
                    description = "Уведомление, если вы не заходили 3 дня",
                    isEnabled = true
                ),
                NotificationSetting(
                    id = NotificationSetting.WEEKLY_SUMMARY_ID,
                    title = "Недельная статистика",
                    description = "Сводка вашей активности за неделю по воскресеньям в 18:00",
                    isEnabled = false,
                    hour = 18,
                    minute = 0
                ),
                NotificationSetting(
                    id = NotificationSetting.NEW_BREEDS_ID,
                    title = "Новые породы",
                    description = "Уведомления о новых породах кошек",
                    isEnabled = true
                ),
                NotificationSetting(
                    id = NotificationSetting.FAVORITE_REMINDER_ID,
                    title = "Напоминание об избранном",
                    description = "Периодически напоминает о ваших избранных котиках",
                    isEnabled = false
                )
            )

            defaultSettings.forEach { setting ->
                dao.insertSetting(setting)
            }
            Log.d("NotificationRepository", "Default notification settings initialized")
        }
    }

    // Отправка тестового уведомления - ПРОСТАЯ ВЕРСИЯ
    fun sendTestNotification(settingId: String) {
        try {
            val (title, message) = when (settingId) {
                NotificationSetting.DAILY_REMINDER_ID ->
                    Pair("🐱 Тест: Ежедневное напоминание", "Сегодня вы посмотрели 5 котиков. Посмотрите ещё 5 чтобы достичь цели!")

                NotificationSetting.INACTIVITY_REMINDER_ID ->
                    Pair("😿 Тест: Напоминание о неактивности", "Вы не заходили 3 дня. Новые котики ждут!")

                NotificationSetting.WEEKLY_SUMMARY_ID ->
                    Pair("📊 Тест: Недельная статистика", "За неделю: 42 котика, 7 в избранном, 3 новые породы")

                NotificationSetting.NEW_BREEDS_ID ->
                    Pair("🎉 Тест: Новые породы", "Добавлено 5 новых пород кошек!")

                NotificationSetting.FAVORITE_REMINDER_ID ->
                    Pair("❤️ Тест: Избранное", "Вспомните ваших любимых котиков!")

                else -> Pair("Тестовое уведомление", "Проверка работы уведомлений")
            }

            Log.d("NotificationRepository", "Sending test notification: $title - $message")

            // ПРОСТОЙ ВЫЗОВ через WorkManager
            val workRequest = OneTimeWorkRequestBuilder<CatNotificationWorker>()
                .setInputData(workDataOf(
                    "title" to title,
                    "message" to message
                ))
                .setInitialDelay(0, TimeUnit.SECONDS) // Немедленно
                .build()

            WorkManager.getInstance(context).enqueue(workRequest)
            Log.d("NotificationRepository", "WorkManager request enqueued successfully")

        } catch (e: Exception) {
            Log.e("NotificationRepository", "Failed to send test notification: ${e.message}", e)
        }
    }

    // Проверяем условия для отправки умных уведомлений
    suspend fun checkAndSendSmartNotifications() {
        val notificationSettings = dao.getAllSettings().first()

        notificationSettings.forEach { setting ->
            if (setting.isEnabled) {
                when (setting.id) {
                    NotificationSetting.DAILY_REMINDER_ID -> checkDailyReminder()
                    NotificationSetting.INACTIVITY_REMINDER_ID -> checkInactivityReminder()
                    NotificationSetting.WEEKLY_SUMMARY_ID -> checkWeeklySummary()
                    // NEW_BREEDS_ID и FAVORITE_REMINDER_ID вызываются по событиям
                }
            }
        }
    }

    // Проверка ежедневного напоминания
    private suspend fun checkDailyReminder() {
        val dailyStats = getDailyStats()

        // Отправляем напоминание только если пользователь не достиг цели
        if (dailyStats.imagesViewedToday < dailyStats.dailyGoal) {
            sendDailyReminder(dailyStats)
        }
    }

    // Ежедневное напоминание с прогрессом
    private fun sendDailyReminder(dailyStats: DailyStats) {
        val remaining = dailyStats.dailyGoal - dailyStats.imagesViewedToday
        val workRequest = OneTimeWorkRequestBuilder<CatNotificationWorker>()
            .setInputData(
                workDataOf(
                    "title" to "🐱 Не забудьте про котиков!",
                    "message" to "Сегодня вы посмотрели ${dailyStats.imagesViewedToday} котиков. " +
                            "Осталось посмотреть ещё $remaining чтобы достичь дневной цели!",
                    "type" to "daily_reminder"
                )
            )
            .setInitialDelay(0, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
    }

    // Проверка неактивности
    private suspend fun checkInactivityReminder() {
        val lastActivity = getLastUserActivity()
        val threeDaysAgo = System.currentTimeMillis() - (3 * 24 * 60 * 60 * 1000)

        if (lastActivity < threeDaysAgo) {
            sendInactivityReminder()
        }
    }

    private fun sendInactivityReminder() {
        val workRequest = OneTimeWorkRequestBuilder<CatNotificationWorker>()
            .setInputData(
                workDataOf(
                    "title" to "😿 Мы скучаем по вам!",
                    "message" to "Вы давно не смотрели котиков. Новые милые фото уже ждут!",
                    "type" to "inactivity_reminder"
                )
            )
            .setInitialDelay(0, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
    }

    // Недельная статистика
    private suspend fun checkWeeklySummary() {
        // Проверяем, что сегодня воскресенье
        val calendar = java.util.Calendar.getInstance()
        if (calendar.get(java.util.Calendar.DAY_OF_WEEK) == java.util.Calendar.SUNDAY) {
            val weeklyStats = getWeeklyStats()
            sendWeeklySummary(weeklyStats)
        }
    }

    private fun sendWeeklySummary(weeklyStats: WeeklyStats) {
        val workRequest = OneTimeWorkRequestBuilder<CatNotificationWorker>()
            .setInputData(
                workDataOf(
                    "title" to "📊 Ваша неделя с котиками",
                    "message" to "За неделю вы:\n" +
                            "• Посмотрели ${weeklyStats.imagesViewed} котиков\n" +
                            "• Добавили ${weeklyStats.favoritesAdded} в избранное\n" +
                            "• Открыли ${weeklyStats.breedsDiscovered} новых пород\n" +
                            "• Провели ${weeklyStats.timeSpent} с нашим приложением",
                    "type" to "weekly_summary"
                )
            )
            .setInitialDelay(0, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
    }

    // Уведомление о новых породах (вызывается при обновлении данных)
    fun sendNewBreedsNotification(newBreedsCount: Int) {
        val message = when {
            newBreedsCount == 1 -> "Добавлена 1 новая порода кошек!"
            newBreedsCount < 5 -> "Добавлено $newBreedsCount новые породы кошек!"
            else -> "Добавлено $newBreedsCount новых пород кошек!"
        }

        val workRequest = OneTimeWorkRequestBuilder<CatNotificationWorker>()
            .setInputData(
                workDataOf(
                    "title" to "🎉 Новые породы котиков!",
                    "message" to message,
                    "type" to "new_breeds"
                )
            )
            .setInitialDelay(0, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
    }

    // Напоминание о избранном
    fun sendFavoriteReminder() {
        val workRequest = OneTimeWorkRequestBuilder<CatNotificationWorker>()
            .setInputData(
                workDataOf(
                    "title" to "❤️ Ваши любимые котики",
                    "message" to "Пересмотрите ваших избранных котиков! ❤️",
                    "type" to "favorite_reminder"
                )
            )
            .setInitialDelay(0, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
    }

    // Получение времени последней активности пользователя
    private suspend fun getLastUserActivity(): Long {
        val images = database.catImageDao().getAllImages().first()
        return images.maxByOrNull { it.lastUpdated }?.lastUpdated ?: 0L
    }

    // Временные методы для статистики (заглушки)
    private suspend fun getWeeklyStats(): WeeklyStats {
        return WeeklyStats(
            imagesViewed = (20..50).random(),
            favoritesAdded = (1..10).random(),
            breedsDiscovered = (0..5).random(),
            timeSpent = "${(1..4).random()} часа"
        )
    }

    private suspend fun getDailyStats(): DailyStats {
        return DailyStats(
            imagesViewedToday = (0..8).random(),
            dailyGoal = 10
        )
    }

    suspend fun clearAll() {
        dao.clearAll()
    }
}

// Модели данных для статистики
data class WeeklyStats(
    val imagesViewed: Int,
    val favoritesAdded: Int,
    val breedsDiscovered: Int,
    val timeSpent: String
)

data class DailyStats(
    val imagesViewedToday: Int,
    val dailyGoal: Int
)