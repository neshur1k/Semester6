package com.example.angatkinmirea

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
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

    var moscowTemp by remember { mutableStateOf<Int?>(null) }
    var londonTemp by remember { mutableStateOf<Int?>(null) }
    var newYorkTemp by remember { mutableStateOf<Int?>(null) }

    var moscowState by remember { mutableStateOf<WorkInfo.State?>(null) }
    var londonState by remember { mutableStateOf<WorkInfo.State?>(null) }
    var newYorkState by remember { mutableStateOf<WorkInfo.State?>(null) }

    val workManager = remember { WorkManager.getInstance(context) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        WeatherCard("Москва", moscowTemp, moscowState)
        WeatherCard("Лондон", londonTemp, londonState)
        WeatherCard("Нью-Йорк", newYorkTemp, newYorkState)

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val cities = listOf("Moscow", "London", "NewYork")

                cities.forEach { city ->
                    val request = OneTimeWorkRequestBuilder<CityWeatherWorker>()
                        .setInputData(workDataOf("city" to city))
                        .build()

                    workManager.enqueue(request)

                    workManager.getWorkInfoByIdLiveData(request.id)
                        .observeForever { workInfo ->
                            if (workInfo != null) {
                                val temp = workInfo.outputData.getInt("temp", 0)

                                when (city) {
                                    "Moscow" -> {
                                        moscowState = workInfo.state
                                        if (workInfo.state.isFinished) moscowTemp = temp
                                    }
                                    "London" -> {
                                        londonState = workInfo.state
                                        if (workInfo.state.isFinished) londonTemp = temp
                                    }
                                    "NewYork" -> {
                                        newYorkState = workInfo.state
                                        if (workInfo.state.isFinished) newYorkTemp = temp
                                    }
                                }
                            }
                        }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Собрать прогноз")
        }
    }
}

@Composable
fun WeatherCard(city: String, temp: Int?, state: WorkInfo.State?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = city,
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = temp?.let { "$it°C" } ?: "—",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {

                if (state == WorkInfo.State.RUNNING) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Text(
                    text = when (state) {
                        WorkInfo.State.ENQUEUED -> "Ожидание"
                        WorkInfo.State.RUNNING -> "Загружается"
                        WorkInfo.State.SUCCEEDED -> "Готово"
                        WorkInfo.State.FAILED -> "Ошибка"
                        else -> "—"
                    }
                )
            }
        }
    }
}