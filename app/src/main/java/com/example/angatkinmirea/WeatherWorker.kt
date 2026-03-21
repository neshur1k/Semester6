package com.example.angatkinmirea

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Observer
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.delay
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

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

class WeatherWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {

        val cities = listOf("Moscow", "London", "NewYork")
        val workManager = WorkManager.getInstance(applicationContext)

        val requests = cities.map { city ->
            OneTimeWorkRequestBuilder<CityWeatherWorker>()
                .setInputData(workDataOf("city" to city))
                .build()
        }

        workManager.enqueue(requests)

        // 🔥 ЖДЁМ пока все завершатся
        var allFinished = false
        var results: List<WorkInfo>

        while (!allFinished) {
            delay(500)

            results = requests.map {
                workManager.getWorkInfoById(it.id).get()
            }

            allFinished = results.all { it.state.isFinished }
        }

        // теперь точно есть данные
        val finalResults = requests.map {
            workManager.getWorkInfoById(it.id).get()
        }

        val moscow = finalResults.find {
            it.outputData.getString("city") == "Moscow"
        }?.outputData?.getInt("temp", 0) ?: 0

        val london = finalResults.find {
            it.outputData.getString("city") == "London"
        }?.outputData?.getInt("temp", 0) ?: 0

        val newYork = finalResults.find {
            it.outputData.getString("city") == "NewYork"
        }?.outputData?.getInt("temp", 0) ?: 0

        val avg = listOf(moscow, london, newYork).average().toInt()

        return Result.success(
            workDataOf(
                "Moscow" to moscow,
                "London" to london,
                "NewYork" to newYork,
                "avg" to avg
            )
        )
    }
}