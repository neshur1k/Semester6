package com.example.angatkinmirea
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.angatkinmirea.ui.theme.AngatkinMIREATheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AngatkinMIREATheme {
                Hello()
            }
        }
    }
}

@Composable
fun Hello(modifier: Modifier = Modifier) {
    Text(
        text = "Hello World",
        modifier = modifier
    )
}
