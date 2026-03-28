package com.example.angatkinmirea

import android.content.Context
import android.content.pm.ServiceInfo
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.delay
import org.json.JSONObject

class CityWeatherWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val city = inputData.getString("city") ?: return Result.failure()

        val notification = NotificationUtils.buildNotification(
            applicationContext,
            "Загружаем $city..."
        )

        setForeground(
            ForegroundInfo(
                NotificationUtils.NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        )

        delay(2000)

        val (temp, condition) = loadWeatherFromJson(city)

        return Result.success(
            workDataOf(
                city to temp,
                "${city}_condition" to condition
            )
        )
    }

    private fun loadWeatherFromJson(city: String): Pair<Int, String> {
        val json = applicationContext.assets.open("weather.json")
            .bufferedReader()
            .use { it.readText() }

        val obj = JSONObject(json)
        val cityObj = obj.getJSONObject(city)

        val temp = cityObj.getInt("temp")
        val condition = cityObj.getString("condition")

        return temp to condition
    }
}