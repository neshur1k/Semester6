package com.example.angatkinmirea
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.angatkinmirea.ui.theme.AngatkinMIREATheme
import kotlin.math.roundToInt

class CompassActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AngatkinMIREATheme {
                CompassScreen()
            }
        }
    }
}

@Composable
fun CompassScreen(viewModel: CompassViewModel = viewModel()) {

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
    ) {

        val azimuth by viewModel.azimuth.collectAsState()

        DisposableEffect(Unit) {
            viewModel.start()
            onDispose { viewModel.stop() }
        }

        if (!viewModel.isSensorAvailable) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Устройство не поддерживает датчик ориентации",
                    color = Color.Red
                )
            }
        } else {

            val animatedAngle by animateFloatAsState(
                targetValue = -azimuth,
                animationSpec = tween(500),
                label = ""
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text("Компас", style = MaterialTheme.typography.headlineMedium, color = Color.White)

                Spacer(modifier = Modifier.height(24.dp))

                Box(
                    modifier = Modifier.size(280.dp),
                    contentAlignment = Alignment.Center
                ) {

                    Canvas(modifier = Modifier.fillMaxSize(0.8f)) {

                        // круг
                        drawCircle(
                            color = Color.Gray,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(4f)
                        )

                        // стрелка
                        rotate(animatedAngle) {

                            // север (красный)
                            drawLine(
                                color = Color.Red,
                                start = center,
                                end = center.copy(y = center.y - size.minDimension * 0.4f),
                                strokeWidth = 8f
                            )

                            // юг (серый)
                            drawLine(
                                color = Color.Gray,
                                start = center,
                                end = center.copy(y = center.y + size.minDimension * 0.4f),
                                strokeWidth = 8f
                            )
                        }
                    }

                    Text(
                        text = "N",
                        modifier = Modifier.align(Alignment.TopCenter),
                        color = Color.Red
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Азимут: ${azimuth.roundToInt()}°",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White
                )
            }
        }
    }
}