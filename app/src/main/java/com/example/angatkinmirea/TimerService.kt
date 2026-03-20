package com.example.angatkinmirea
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

class TimerService(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        createChannel(applicationContext)
        val seconds = inputData.getLong("SECONDS", 0)

        Thread.sleep(seconds * 1000)

        val notification = NotificationCompat.Builder(applicationContext, "timer_channel")
            .setSmallIcon(R.drawable.circle)
            .setContentTitle("Таймер завершён")
            .setContentText("Время вышло!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager

        notificationManager.notify(1, notification)

        return Result.success()
    }
}

fun startTimer(context: Context, seconds: Long) {

    val workRequest = OneTimeWorkRequestBuilder<TimerService>()
        .setInputData(
            androidx.work.workDataOf("SECONDS" to seconds)
        )
        .build()

    WorkManager.getInstance(context).enqueue(workRequest)
}

fun createChannel(context: Context) {
    val channel = android.app.NotificationChannel(
        "timer_channel",
        "Timer",
        android.app.NotificationManager.IMPORTANCE_HIGH
    )

    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
    manager.createNotificationChannel(channel)
}
