package com.example.angatkinmirea

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.angatkinmirea.ui.theme.AngatkinMIREATheme
import kotlinx.coroutines.delay

class RandomActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AngatkinMIREATheme {
                RandomScreen(this)
            }
        }
    }
}

@Composable
fun RandomScreen(context: Context) {
    var service by remember { mutableStateOf<RandomService?>(null) }
    var bound by remember { mutableStateOf(false) }
    var number by remember { mutableStateOf(-1) }

    val connection = remember {
        object : ServiceConnection {

            override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                val localBinder = binder as RandomService.LocalBinder
                service = localBinder.getService()
                bound = true
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                bound = false
                service = null
            }
        }
    }

    LaunchedEffect(bound) {
        while (bound) {
            service?.let {
                number = it.getNumber()
            }
            delay(500)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (number == -1) "-" else "$number",
            fontSize = 80.sp
        )

        Spacer(modifier = Modifier.width(60.dp))

        Button(onClick = {
            val intent = Intent(context, RandomService::class.java)
            context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }) {
            Text("Подключиться")
        }

        Spacer(modifier = Modifier.width(60.dp))

        Button(onClick = {
            if (bound) {
                context.unbindService(connection)
                bound = false
            }
        }) {
            Text("Отключиться")
        }
    }
}