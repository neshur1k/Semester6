package com.example.angatkinmirea.module4
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.angatkinmirea.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

object NotificationHelper {
    private const val CHANNEL_ID = "Task5"
    private const val CHANNEL_NAME = "Stopwatch"

    fun createNotificationChannel(context: Context) {
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_ID, CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW)
        notificationManager.createNotificationChannel(channel)
    }

    fun createMessage(context: Context, text: String) =
        NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.circle)
            .setContentTitle("Секундомер работает")
            .setContentText(text)
            .build()
}

class StopwatchService : Service() {
    private var seconds = 0
    private var isRunning = false
    private val SERVICE_ID = 100
    private val serviceScope = CoroutineScope(Dispatchers.Default)
    private var stopwatchJob: Job? = null

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        val secondsFlow = MutableStateFlow(0)
    }

    override fun onBind(intent: Intent): IBinder {
        return Binder()
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("RRR", "onCreate()")
    }

    private fun startStopwatch() {
        if (stopwatchJob != null) return
        stopwatchJob = serviceScope.launch {
            while (true) {
                delay(1000)
                seconds++
                secondsFlow.value = seconds
                updateNotification()
                Log.d("TimerService", "Seconds: $seconds")
            }
        }
    }

    private fun pauseStopwatch() {
        stopwatchJob?.cancel()
        stopwatchJob = null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        NotificationHelper.createNotificationChannel(this)
        super.startForeground(SERVICE_ID,
            NotificationHelper.createMessage(this, "Прошло $seconds секунд")
        )
        when (intent?.action) {
            ACTION_START -> startStopwatch()
            ACTION_PAUSE -> pauseStopwatch()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        stopwatchJob?.cancel()
        stopwatchJob = null
        super.onDestroy()
    }

    private fun updateNotification() {
        val notification = NotificationCompat.Builder(this, "Task5")
            .setSmallIcon(R.drawable.circle)
            .setContentTitle("Секундомер работает")
            .setContentText("Прошло $seconds секунд")
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(SERVICE_ID, notification)
    }
}
