package com.example.funny_cats.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notification_settings")
data class NotificationSetting(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val isEnabled: Boolean = false,
    val hour: Int = 12, // Время для уведомлений по расписанию
    val minute: Int = 0
) {
    companion object {
        // Ежедневное напоминание - в 20:00, если пользователь не достиг дневной цели
        const val DAILY_REMINDER_ID = "daily_reminder"

        // Напоминание о неактивности - если пользователь не заходил 3 дня
        const val INACTIVITY_REMINDER_ID = "inactivity_reminder"

        // Недельная статистика - в воскресенье в 18:00
        const val WEEKLY_SUMMARY_ID = "weekly_summary"

        // Новые породы - при появлении новых данных
        const val NEW_BREEDS_ID = "new_breeds"

        // Избранное напоминание - раз в неделю показать случайное избранное
        const val FAVORITE_REMINDER_ID = "favorite_reminder"
    }
}