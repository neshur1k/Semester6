package com.example.angatkinmirea
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.angatkinmirea.data.repository.TodoRepositoryImpl
import com.example.angatkinmirea.domain.repository.TodoRepository
import com.example.angatkinmirea.domain.usecase.GetTodosUseCase
import com.example.angatkinmirea.domain.usecase.ToggleTodoUseCase
import com.example.angatkinmirea.navigation.NavGraph
import com.example.angatkinmirea.presentation.ui.theme.AngatkinMIREATheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AngatkinMIREATheme {
                NavGraph(context = this)
            }
        }
    }
}

@Composable
fun Hello(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "",
        modifier = modifier
    )
}
