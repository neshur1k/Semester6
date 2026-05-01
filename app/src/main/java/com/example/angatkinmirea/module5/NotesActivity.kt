package com.example.angatkinmirea.module5

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

// ===================== MODEL =====================
data class Note(
    val fileName: String,
    val text: String,
    val timestamp: Long
)

// ===================== REPOSITORY =====================
class NotesRepository {

    fun saveNote(context: Context, title: String?, text: String): Note {
        val time = System.currentTimeMillis()
        val safeTitle = title?.replace("\\s+".toRegex(), "_") ?: ""

        val fileName = "timestamp_${time}" +
                (if (safeTitle.isEmpty()) "" else "_$safeTitle") + ".txt"

        context.openFileOutput(fileName, Context.MODE_PRIVATE).use {
            it.write(text.toByteArray())
        }

        return Note(fileName, text, time)
    }

    fun loadNotes(context: Context): List<Note> {
        val dir = context.filesDir

        val files = dir.listFiles()?.filter {
            it.name.startsWith("timestamp_") && it.name.endsWith(".txt")
        } ?: return emptyList()

        val list = mutableListOf<Note>()

        for (file in files) {
            try {
                val text = file.readText()
                val timestamp = extractTimestamp(file.name)

                if (timestamp <= 0) continue

                list.add(Note(file.name, text, timestamp))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return list.sortedByDescending { it.timestamp }
    }

    fun updateNote(context: Context, fileName: String, text: String) {
        context.openFileOutput(fileName, Context.MODE_PRIVATE).use {
            it.write(text.toByteArray())
        }
    }

    fun updateNoteWithRename(
        context: Context,
        oldFileName: String,
        newTitle: String?,
        newText: String,
        timestamp: Long
    ): String {

        val safeTitle = newTitle?.replace("\\s+".toRegex(), "_") ?: ""

        val newFileName = "timestamp_${timestamp}" +
                (if (safeTitle.isEmpty()) "" else "_$safeTitle") + ".txt"

        val oldFile = File(context.filesDir, oldFileName)
        val newFile = File(context.filesDir, newFileName)

        // удаляем старый файл
        if (oldFile.exists()) oldFile.delete()

        // создаём новый
        context.openFileOutput(newFileName, Context.MODE_PRIVATE).use {
            it.write(newText.toByteArray())
        }

        return newFileName
    }

    fun extractTimestamp(fileName: String): Long {
        val parts = fileName.removeSuffix(".txt").split("_")
        return parts.getOrNull(1)?.toLongOrNull() ?: -1L
    }

    fun deleteNote(context: Context, fileName: String) {
        val file = File(context.filesDir, fileName)
        if (file.exists()) file.delete()
    }
}

// ===================== VIEWMODEL =====================
class NotesViewModel : ViewModel() {

    private val repo = NotesRepository()

    private val _notes = MutableLiveData<List<Note>>(emptyList())
    val notes: LiveData<List<Note>> = _notes

    fun init(context: Context) {
        _notes.value = repo.loadNotes(context)
    }

    fun addNote(context: Context, title: String?, text: String) {
        val note = repo.saveNote(context, title, text)

        val current = _notes.value?.toMutableList() ?: mutableListOf()
        current.add(0, note)
        _notes.value = current
    }

    fun updateNote(
        context: Context,
        note: Note,
        newTitle: String?,
        newText: String
    ) {
        val newFileName = repo.updateNoteWithRename(
            context,
            note.fileName,
            newTitle,
            newText,
            note.timestamp
        )

        val current = _notes.value?.toMutableList() ?: mutableListOf()

        val index = current.indexOfFirst { it.fileName == note.fileName }
        if (index != -1) {
            current[index] = note.copy(
                fileName = newFileName,
                text = newText
            )
            _notes.value = current
        }
    }

    fun deleteNote(context: Context, note: Note) {
        repo.deleteNote(context, note.fileName)

        val current = _notes.value?.toMutableList() ?: mutableListOf()
        current.removeAll { it.fileName == note.fileName }
        _notes.value = current
    }
}

// ===================== UI =====================
class NotesActivity : ComponentActivity() {

    private val viewModel by viewModels<NotesViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                MainScreen(viewModel, applicationContext)
            }
        }
    }
}

@Composable
fun MainScreen(viewModel: NotesViewModel, context: Context) {

    var screen by remember { mutableStateOf("list") }
    var selectedNote by remember { mutableStateOf<Note?>(null) }

    when (screen) {

        "list" -> NotesScreen(
            viewModel,
            context,
            onAddClick = { screen = "add" },
            onNoteClick = {
                selectedNote = it
                screen = "edit"
            }
        )

        "add" -> AddNoteScreen(
            viewModel,
            context,
            onBack = { screen = "list" }
        )

        "edit" -> selectedNote?.let { note ->
            EditNoteScreen(
                viewModel = viewModel,
                context = context,
                note = note,
                onBack = { screen = "list" }
            )
        }
    }
}

@Composable
fun NotesScreen(
    viewModel: NotesViewModel,
    context: Context,
    onAddClick: () -> Unit,
    onNoteClick: (Note) -> Unit
) {

    val notes by viewModel.notes.observeAsState(emptyList())

    LaunchedEffect(Unit) {
        viewModel.init(context)
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Text("+")
            }
        }
    ) { padding ->

        if (notes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("У вас пока нет записей\nНажмите +, чтобы создать первую")
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding)
            ) {
                items(
                    items = notes,
                    key = { it.fileName }
                ) { note ->
                    NoteItem(
                        note = note,
                        onClick = { onNoteClick(note) },
                        onDelete = { viewModel.deleteNote(context, note) }
                    )
                }
            }
        }
    }
}

@Composable
fun NoteItem(
    note: Note,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val title = extractTitle(note.fileName)
    val date = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(note.timestamp))
    val preview = if (note.text.length > 40) {
        note.text.take(40) + "..."
    } else note.text

    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant, // 🔥 светло-серый
                    shape = MaterialTheme.shapes.medium
                )
                .combinedClickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick,
                    onLongClick = { expanded = true }
                )
                .padding(16.dp)
        ) {
            if (title.isNotBlank()) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(4.dp))
            }
            Text(date, style = MaterialTheme.typography.labelSmall)
            Spacer(Modifier.height(4.dp))
            Text(preview, style = MaterialTheme.typography.bodyLarge)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Удалить") },
                onClick = {
                    expanded = false
                    onDelete()
                }
            )
        }
    }
}

@Composable
fun AddNoteScreen(
    viewModel: NotesViewModel,
    context: Context,
    onBack: () -> Unit
) {

    var title by remember { mutableStateOf("") }
    var text by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Заголовок (опционально)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Текст записи") },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row {
            Button(
                onClick = {
                    viewModel.addNote(context, title, text)
                    onBack()
                }
            ) {
                Text("Сохранить")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(onClick = onBack) {
                Text("Назад")
            }
        }
    }
}

@Composable
fun EditNoteScreen(
    viewModel: NotesViewModel,
    context: Context,
    note: Note,
    onBack: () -> Unit
) {

    var title by remember { mutableStateOf(extractTitle(note.fileName)) }
    var text by remember { mutableStateOf(note.text) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Заголовок") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )

        Spacer(Modifier.height(12.dp))

        Row {
            Button(
                onClick = {
                    viewModel.updateNote(context, note, title, text)
                    onBack()
                }
            ) {
                Text("Сохранить")
            }

            Spacer(Modifier.width(8.dp))

            Button(onClick = onBack) {
                Text("Назад")
            }
        }
    }
}

fun extractTitle(fileName: String): String {
    val parts = fileName.removeSuffix(".txt").split("_")
    return parts.getOrNull(2)?.replace("_", " ")?.trim().orEmpty()
}