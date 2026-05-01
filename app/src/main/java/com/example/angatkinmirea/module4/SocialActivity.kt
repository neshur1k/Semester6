package com.example.angatkinmirea.module4

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.angatkinmirea.ui.theme.AngatkinMIREATheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class Post(
    val id: Int,
    val userId: Int,
    val title: String,
    val body: String,
    val avatarUrl: String
)

@Serializable
data class Comment(
    val postId: Int,
    val id: Int,
    val name: String,
    val body: String
)

sealed class LoadState<out T> {
    object Loading : LoadState<Nothing>()
    data class Ready<T>(val data: T) : LoadState<T>()
    data class Error(val exception: Throwable) : LoadState<Nothing>()
}

class SocialActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AngatkinMIREATheme {
                PostsScreen(this)
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

@Composable
fun PostsScreen(context: Context) {
    val scope = rememberCoroutineScope()

    var postsState by remember { mutableStateOf<List<Pair<Post, LoadState<Pair<String?, List<Comment>>>>>>(emptyList()) }
    var loadJob by remember { mutableStateOf<Job?>(null) }

    fun loadPostsData() {
        loadJob?.cancel()
        postsState = emptyList()

        loadJob = scope.launch {
            val posts = loadPosts(context).shuffled().take((8..15).random())

            postsState = posts.map { it to LoadState.Loading }

            posts.forEach { post ->
                launch {
                    val currentJob = coroutineContext[Job]
                    supervisorScope {
                        val avatarDeferred = async { loadAvatar(post.avatarUrl) }
                        val commentsDeferred = async { loadComments(context, post.id) }

                        val avatar = try { avatarDeferred.await() } catch (e: Exception) { null }
                        val comments = try { commentsDeferred.await() } catch (e: Exception) { emptyList() }

                        if (currentJob?.isActive == true) {
                            val index = postsState.indexOfFirst { it.first.id == post.id }
                            if (index != -1) {
                                postsState = postsState.toMutableList().also { list ->
                                    list[index] = if (avatar != null && comments.isNotEmpty()) {
                                        post to LoadState.Ready(avatar to comments)
                                    } else {
                                        post to LoadState.Error(Exception())
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) { loadPostsData() }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Button(onClick = { loadPostsData() }, modifier = Modifier.fillMaxWidth()) {
            Text("Обновить")
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(postsState) { (post, state) ->
                PostCard(post, state)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun PostCard(post: Post, state: LoadState<Pair<String?, List<Comment>>>) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(post.title, fontWeight = FontWeight.Bold)
            Text(post.body)

            when (state) {
                is LoadState.Loading -> {
                    Text("Loading", fontStyle = FontStyle.Italic)
                    Box(modifier = Modifier.padding(50.dp))
                }
                is LoadState.Ready -> {
                    Text("Ready", fontStyle = FontStyle.Italic)
                    val (avatarUrl, comments) = state.data
                    Box(modifier = Modifier.padding(50.dp)) {
                        if (avatarUrl != null) {
                            Text(avatarUrl)
                        }
                    }
                    Column {
                        comments.forEach {
                            Text("${it.name}: ${it.body}")
                        }
                    }
                }
                is LoadState.Error -> {
                    Text("Error", fontStyle = FontStyle.Italic)
                    Box(modifier = Modifier.padding(50.dp)) {
                        Text("X")
                    }
                }
            }
        }
    }
}

suspend fun loadPosts(context: Context): List<Post> = withContext(Dispatchers.IO) {
    delay(500)
    val jsonString = context.assets.open("social_posts.json").bufferedReader().use {
        it.readText()
    }
    val posts = Json.decodeFromString<List<Post>>(jsonString)
    posts
}

suspend fun loadComments(context: Context, postId: Int): List<Comment> = withContext(Dispatchers.IO) {
    delay(1000)
    val jsonString = context.assets.open("comments.json").bufferedReader().use {
        it.readText()
    }
    val comments = Json.decodeFromString<List<Comment>>(jsonString)
    comments.filter { it.postId == postId }
}

suspend fun loadAvatar(url: String): String = withContext(Dispatchers.IO) {
    delay(1000)
    if (url.contains("error")) throw Exception()
    url
}