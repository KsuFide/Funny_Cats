package com.example.funny_cats.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.funny_cats.R
import com.example.funny_cats.ui.MainActivity

class CatNotificationWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        return try {
            val title = inputData.getString("title") ?: "🐱 Время для котиков!"
            val message = inputData.getString("message") ?: "Посмотрите новых котиков!"

            println("🐱 [CatNotificationWorker] Creating notification: $title")
            showNotification(title, message)
            println("🐱 [CatNotificationWorker] Notification created successfully")
            Result.success()
        } catch (e: Exception) {
            println("❌ [CatNotificationWorker] Error: ${e.message}")
            e.printStackTrace()
            Result.failure()
        }
    }

    private fun showNotification(title: String, message: String) {
        val notificationManager = applicationContext
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Создаем канал (обязательно для Android 8.0+)
        createNotificationChannel(notificationManager)

        // Intent для открытия приложения - упрощаем
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            action = System.currentTimeMillis().toString() // Уникальное действие
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            System.currentTimeMillis().toInt(), // Уникальный requestCode
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Создаем уведомление с более простой конфигурацией
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(getNotificationIcon())
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setLights(Color.YELLOW, 1000, 1000)
            .setVibrate(longArrayOf(0, 500, 250, 500)) // Вибрация: пауза, вибро, пауза, вибро
            .build()

        // Показываем уведомление с уникальным ID
        val notificationId = System.currentTimeMillis().toInt()
        println("🐱 [CatNotificationWorker] Showing notification with ID: $notificationId")
        notificationManager.notify(notificationId, notification)
    }

    private fun getNotificationIcon(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Для Android 8+ используем иконку из ресурсов
            R.drawable.ic_notification
        } else {
            // Для старых версий используем стандартную иконку
            android.R.drawable.ic_dialog_info
        }
    }

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        // Проверяем, не создан ли уже канал
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val existingChannel = notificationManager.getNotificationChannel(CHANNEL_ID)
            if (existingChannel == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = CHANNEL_DESCRIPTION
                    enableLights(true)
                    lightColor = Color.YELLOW
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 500, 250, 500)
                    setShowBadge(true)
                    lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                }
                notificationManager.createNotificationChannel(channel)
                println("🐱 [CatNotificationWorker] Notification channel created")
            } else {
                println("🐱 [CatNotificationWorker] Notification channel already exists")
            }
        }
    }

    companion object {
        const val CHANNEL_ID = "cat_notifications"
        const val CHANNEL_NAME = "Уведомления о котиках"
        const val CHANNEL_DESCRIPTION = "Уведомления о новых котиках и напоминания"
    }
}