package com.example.angatkinmirea.module4

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.delay

object NotificationUtils {
    const val CHANNEL_ID = "weather_channel"
    const val NOTIFICATION_ID = 1

    fun createChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Weather",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    fun buildNotification(context: Context, text: String): Notification {
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Прогноз погоды")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setOngoing(true)
            .build()
    }

    fun show(context: Context, text: String) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification(context, text))
    }
}

class ReportWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {

        val moscow = inputData.getIntArray("Moscow")?.firstOrNull() ?: 0
        val london = inputData.getIntArray("London")?.firstOrNull() ?: 0
        val newYork = inputData.getIntArray("NewYork")?.firstOrNull() ?: 0

        NotificationUtils.show(
            applicationContext,
            "Все данные получены, формируем отчёт..."
        )

        delay(1000)

        val avg = listOf(moscow, london, newYork).average().toInt()

        NotificationUtils.show(
            applicationContext,
            "Отчёт готов! Средняя температура +${avg}°C"
        )

        return Result.success()
    }
}