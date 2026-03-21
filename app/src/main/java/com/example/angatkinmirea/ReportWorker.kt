package com.example.angatkinmirea

import android.content.Context
import android.content.pm.ServiceInfo
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import kotlinx.coroutines.delay

class ReportWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {

        val moscow = inputData.getInt("Moscow", 0)
        val london = inputData.getInt("London", 0)
        val newYork = inputData.getInt("NewYork", 0)

        NotificationUtils.show(
            applicationContext,
            "Все данные получены, формируем отчёт…"
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