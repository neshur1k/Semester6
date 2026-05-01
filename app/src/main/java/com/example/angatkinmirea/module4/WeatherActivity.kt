package com.example.angatkinmirea.module4

import android.content.Context
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.work.ArrayCreatingInputMerger
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.angatkinmirea.ui.theme.AngatkinMIREATheme
import java.util.UUID

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

    var moscowCondition by remember { mutableStateOf<String?>(null) }
    var londonCondition by remember { mutableStateOf<String?>(null) }
    var newYorkCondition by remember { mutableStateOf<String?>(null) }

    var started by remember { mutableStateOf(false) }
    var workIds by remember { mutableStateOf<List<UUID>>(emptyList()) }
    val states = listOf(moscowState, londonState, newYorkState)
    val inProgressCount = states.count {
        it != null && !it.isFinished
    }
    val allFinished = states.all {
        it == WorkInfo.State.SUCCEEDED
    }
    val statusText = when {
        !started -> "Готов начать"
        allFinished -> "Все данные получены!"
        else -> "Загрузка... ($inProgressCount в процессе)"
    }

    val workManager = remember { WorkManager.getInstance(context) }

    val allLoaded = listOf(moscowState, londonState, newYorkState).all {
        it == WorkInfo.State.SUCCEEDED
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Прогноз погоды",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontSize = 20.sp,
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = statusText,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium
        )

        WeatherCard("Москва", moscowTemp, moscowState)
        WeatherCard("Лондон", londonTemp, londonState)
        WeatherCard("Нью-Йорк", newYorkTemp, newYorkState)

        Spacer(modifier = Modifier.height(16.dp))

        if (allLoaded) {
            val avgTemp = listOfNotNull(
                moscowTemp,
                londonTemp,
                newYorkTemp
            ).average().toInt()

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {

                    Text("Общий прогноз", style = MaterialTheme.typography.titleLarge)

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Москва: $moscowTemp°C, ${moscowCondition ?: "-"}")
                    Text("Лондон: $londonTemp°C, ${londonCondition ?: "-"}")
                    Text("Нью-Йорк: $newYorkTemp°C, ${newYorkCondition ?: "-"}")

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Средняя температура: $avgTemp°C")
                }
            }
        }

        Button(
            onClick = {
                started = true

                val cities = listOf("Moscow", "London", "NewYork")

                val requests = cities.map { city ->
                    OneTimeWorkRequestBuilder<CityWeatherWorker>()
                        .setInputData(workDataOf("city" to city))
                        .build()
                }

                workIds = requests.map { it.id }

                val reportWorker = OneTimeWorkRequestBuilder<ReportWorker>()
                    .setInputMerger(ArrayCreatingInputMerger::class.java)
                    .build()

                workManager
                    .beginWith(requests)
                    .then(reportWorker)
                    .enqueue()

                requests.forEachIndexed { index, request ->
                    val city = cities[index]

                    workManager.getWorkInfoByIdLiveData(request.id)
                        .observeForever { workInfo ->
                            if (workInfo != null) {

                                val temp = workInfo.outputData.getInt(city, 0)
                                val condition = workInfo.outputData.getString("${city}_condition")

                                when (city) {
                                    "Moscow" -> {
                                        moscowState = workInfo.state
                                        if (workInfo.state.isFinished) {
                                            moscowTemp = temp
                                            moscowCondition = condition
                                        }
                                    }
                                    "London" -> {
                                        londonState = workInfo.state
                                        if (workInfo.state.isFinished) {
                                            londonTemp = temp
                                            londonCondition = condition
                                        }
                                    }
                                    "NewYork" -> {
                                        newYorkState = workInfo.state
                                        if (workInfo.state.isFinished) {
                                            newYorkTemp = temp
                                            newYorkCondition = condition
                                        }
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

        if (started && !allFinished) {
            Button(
                onClick = {
                    workIds.forEach { id ->
                        workManager.cancelWorkById(id)
                    }
                    started = false
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Отменить")
            }
        }
    }
}

@Composable
fun WeatherCard(
    city: String,
    temp: Int?,
    state: WorkInfo.State?
) {
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