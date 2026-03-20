package com.example.angatkinmirea

import android.content.Context
import android.content.pm.ServiceInfo
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters

class ReportWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {

        val avg = inputData.getInt("avg", 0)

        val notification = NotificationUtils.buildNotification(
            applicationContext,
            "Отчёт готов! Средняя температура $avg°C"
        )

        setForeground(
            ForegroundInfo(
                NotificationUtils.NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        )

        return Result.success()
    }
}