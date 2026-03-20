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

        val futures = cities.map { city ->
            OneTimeWorkRequestBuilder<CityWeatherWorker>()
                .setInputData(workDataOf("city" to city))
                .build()
        }

        // стартуем все воркеры
        futures.forEach { workManager.enqueue(it) }

        // ждём завершения
        val results = futures.map { workManager.getWorkInfoByIdLiveData(it.id) }
            .map { liveData ->
                suspendCoroutine<WorkInfo> { cont ->
                    val observer = object : Observer<WorkInfo> {
                        override fun onChanged(value: WorkInfo) {
                            if (value.state.isFinished) {
                                cont.resume(value)
                                liveData.removeObserver(this)
                            }
                        }
                    }
                    liveData.observeForever(observer)
                }
            }

        val temps = results.mapNotNull {
            val data = it.outputData
            data.getInt("temp", 0)
        }

        val avg = temps.average().toInt()

        // запускаем финальный воркер
        val reportWork = OneTimeWorkRequestBuilder<ReportWorker>()
            .setInputData(workDataOf("avg" to avg))
            .build()

        workManager.enqueue(reportWork)

        return Result.success()
    }
}