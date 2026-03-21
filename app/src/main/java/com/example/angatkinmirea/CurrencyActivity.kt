package com.example.angatkinmirea
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.angatkinmirea.ui.theme.AngatkinMIREATheme

class CurrencyActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AngatkinMIREATheme {
                CurrencyScreen()
            }
        }
    }
}

@Composable
fun CurrencyScreen(viewModel: CurrencyViewModel = viewModel()) {
    val rate by viewModel.rate.collectAsState()
    val lastUpdate by viewModel.lastUpdate.collectAsState()
    val trend by viewModel.trend.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Курс USD → RUB",
            fontWeight = FontWeight.Bold,
            fontSize = 26.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Text(
                text = String.format("%.2f", rate),
                modifier = Modifier.padding(16.dp),
                fontWeight = FontWeight.Bold,
                fontSize = 60.sp
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        when (trend) {
            1 -> Text(text = "↑", color = Color.Green, fontWeight = FontWeight.Bold, fontSize = 26.sp)
            -1 -> Text(text = "↓", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 26.sp)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Последнее обновление: $lastUpdate",
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            viewModel.generateRate()
        }) {
            Text("Обновить сейчас")
        }
    }
}