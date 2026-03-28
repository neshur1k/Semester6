package com.example.angatkinmirea

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.startForegroundService
import com.example.angatkinmirea.ui.theme.AngatkinMIREATheme

class StopwatchActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AngatkinMIREATheme {
                StopwatchScreen(this)
            }
        }
    }
}

@Composable
fun StopwatchScreen(context: Context) {
    RequestNotificationPermission(context)
    val seconds by StopwatchService.secondsFlow.collectAsState()

    Column (
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "$seconds", fontSize = 80.sp)

        Spacer(modifier = Modifier.width(60.dp))

        Button(onClick = {
            val intent = Intent(context, StopwatchService::class.java).apply {
                action = StopwatchService.ACTION_START
            }
            startForegroundService(context, intent)
        }) {
            Text("Старт")
        }

        Spacer(modifier = Modifier.width(30.dp))

        Button(onClick = {
            val intent = Intent(context, StopwatchService::class.java).apply {
                action = StopwatchService.ACTION_PAUSE
            }
            context.startService(intent)
        }) {
            Text("Стоп")
        }
    }
}

@Composable
fun RequestNotificationPermission(context: Context) {
    LaunchedEffect(Unit) {
        ActivityCompat.requestPermissions(
            context as Activity,
            arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
            1
        )
    }
}