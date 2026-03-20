package com.example.angatkinmirea
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.angatkinmirea.ui.theme.AngatkinMIREATheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class Repo(
    val id: Int,
    val full_name: String,
    val description: String,
    val stargazers_count: Int,
    val language: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AngatkinMIREATheme {
                SearchScreen(context = this)
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

fun <T> CoroutineScope.debounce(
    waitMs: Long = 500L, destinationFunction: (T) -> Unit): (T) -> Unit {
    var debounceJob: Job? = null
    return { param: T ->
        debounceJob?.cancel()
        debounceJob = launch {
            delay(waitMs)
            destinationFunction(param)
        }
    }
}

fun loadRepos(context: Context): List<Repo> {
    val jsonString = context.assets.open("repos.json").use {
        it.readBytes().decodeToString()
    }
    val repos = Json.decodeFromString<List<Repo>>(jsonString)
    return repos
}

@Composable
fun SearchScreen(context: Context) {
    val scope = rememberCoroutineScope()

    var query by remember { mutableStateOf("") }
    var repos by remember { mutableStateOf<List<Repo>>(emptyList()) }
    var filteredRepos by remember { mutableStateOf<List<Repo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var searchJob by remember { mutableStateOf<Job?>(null) }

    LaunchedEffect(Unit) {
        repos = loadRepos(context)
        filteredRepos = repos
    }

    val debouncedSearch = remember {
        scope.debounce<String>(500L) { text ->
            searchJob?.cancel()

            searchJob = scope.launch {
                isLoading = true

                val result = async {
                    withContext(Dispatchers.Default) {
                        delay(300)
                        repos.filter { repo ->
                            repo.full_name.contains(text, ignoreCase = true) ||
                            repo.description.contains(text, ignoreCase = true) ||
                            repo.language.contains(text, ignoreCase = true)
                        }
                    }
                }.await()

                filteredRepos = result
                isLoading = false
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        TextField(
            value = query,
            onValueChange = {
                query = it
                debouncedSearch(it)
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator()
        }

        LazyColumn {
            items(filteredRepos) { repo ->
                RepoItem(repo)
            }
        }
    }
}

@Composable
fun RepoItem(repo: Repo) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(8.dp)
    ) {
        Text(text = repo.full_name, fontWeight = FontWeight.Bold)
        Text(text = repo.description)
        Text(text = "${repo.stargazers_count} | ${repo.language}")
    }
}