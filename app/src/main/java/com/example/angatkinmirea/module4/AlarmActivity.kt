package com.example.angatkinmirea.module4

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import android.Manifest
import androidx.compose.ui.unit.sp

class AlarmActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestNotificationPermission()

        setContent {
            ReminderScreen(this)
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                0
            )
        }
    }
}

@Composable
fun ReminderScreen(context: Context) {

    val prefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
    val enabled = remember { mutableStateOf(prefs.getBoolean("enabled", false)) }

    val text = if (enabled.value) {
        "Напоминание включено"
    } else {
        "Напоминание выключено"
    }

    val nextTime = if (enabled.value) {
        "Следующее напоминание: ${AlarmScheduler.getNextTimeText()}"
    } else {
        "Нажмите, чтобы включить"
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(
                    if (enabled.value) Color.Green else Color.Gray,
                    CircleShape
                )
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(text, fontSize = 30.sp)

        Spacer(modifier = Modifier.height(32.dp))

        Text(nextTime)

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = {
            if (enabled.value) {
                AlarmScheduler.cancel(context)
            } else {
                AlarmScheduler.scheduleNext(context)
            }
            enabled.value = !enabled.value
        }) {
            Text(
                if (enabled.value)
                    "Выключить напоминание"
                else
                    "Включить напоминание"
            )
        }
    }
}