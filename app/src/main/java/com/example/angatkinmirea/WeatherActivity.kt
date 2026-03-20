package com.example.angatkinmirea

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.core.content.ContextCompat.startForegroundService
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.angatkinmirea.ui.theme.AngatkinMIREATheme

class WeatherActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificationUtils.createChannel(this)
        enableEdgeToEdge()
        setContent {
            AngatkinMIREATheme {
                WeatherScreen(this)
            }
        }
    }
}

@Composable
fun WeatherScreen(context: Context) {
    Button(onClick = {
        val request = OneTimeWorkRequestBuilder<WeatherWorker>().build()

        WorkManager.getInstance(context).enqueue(request)
    }) {
        Text("Старт")
    }
}