package com.example.angatkinmirea

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.asFlow
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.angatkinmirea.ui.theme.AngatkinMIREATheme

class PhotoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AngatkinMIREATheme {
                ProcessPhoto(this)
            }
        }
    }
}

@Composable
fun ProcessPhoto(context: Context) {
    val workManager = WorkManager.getInstance(context)
    val photoManager = remember { PhotoWorkManager(context) }

    var status by remember { mutableStateOf("Ожидание") }
    var result by remember { mutableStateOf("") }
    var isRunning by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0) }

    val workInfos by workManager
        .getWorkInfosByTagLiveData("PhotoChain")
        .asFlow()
        .collectAsState(initial = emptyList())

    val isAnyRunning = workInfos.any {
        it.state == WorkInfo.State.RUNNING ||
                it.state == WorkInfo.State.ENQUEUED
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = status)

        Spacer(modifier = Modifier.height(16.dp))

        if (isAnyRunning) {
            LinearProgressIndicator(
                progress = { progress / 100f },
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                workManager.cancelAllWorkByTag("PhotoChain")
                photoManager.chainOneTimeSampleWork("photo.jpg")
                isRunning = true
                result = ""
            },
            enabled = !isAnyRunning
        ) {
            Text("Начать обработку")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = result)
    }

    workInfos.let { list ->
        val validWorks = list.filter {
            it.state != WorkInfo.State.CANCELLED
        }

        val runningWork = validWorks.find {
            it.state == WorkInfo.State.RUNNING
        }

        if (runningWork != null) {
            val step = runningWork.progress.getString("step")

            progress = runningWork.progress.getInt("progress", 0)

            status = when (step) {
                "compress" -> "Сжимаем фото..."
                "watermark" -> "Добавляем водяной знак..."
                "upload" -> "Загружаем..."
                else -> "Обработка..."
            }
        } else {
            val lastWork = validWorks.lastOrNull() ?: return@let

            when (lastWork.state) {
                WorkInfo.State.SUCCEEDED -> {
                    status = "Готово!"
                    result = lastWork.outputData.getString("file") ?: ""
                    progress = 100
                }

                WorkInfo.State.FAILED -> {
                    status = "Ошибка"
                    result = "Что-то пошло не так"
                }

                else -> {}
            }
        }
    }
}