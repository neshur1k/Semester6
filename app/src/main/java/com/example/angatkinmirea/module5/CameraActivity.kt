package com.example.angatkinmirea.module5
import android.Manifest
import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class CameraActivity : ComponentActivity() {

    private val viewModel: PhotoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                PhotoGalleryScreen(viewModel)
            }
        }
    }
}

class PhotoViewModel(application: Application) : AndroidViewModel(application) {

    private val context = getApplication<Application>()

    private val _photos = mutableStateListOf<File>()
    val photos: List<File> = _photos

    init {
        loadPhotos()
    }

    private fun getPhotosDir(): File? {
        return context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    }

    fun loadPhotos() {
        val dir = getPhotosDir() ?: return
        val files = dir.listFiles()?.sortedByDescending { it.lastModified() } ?: emptyList()
        _photos.clear()
        _photos.addAll(files)
    }

    fun addPhoto(file: File) {
        _photos.add(0, file)
    }
}

@Composable
fun PhotoGalleryScreen(viewModel: PhotoViewModel = viewModel()) {

    val context = LocalContext.current
    val photos = viewModel.photos

    var photoUri by remember { mutableStateOf<Uri?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {}

    val takePictureLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            photoUri?.path?.let {
                viewModel.addPhoto(File(it))
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)

                    val file = createImageFile(context)

                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.provider",
                        file
                    )

                    photoUri = uri
                    takePictureLauncher.launch(uri)
                }
            ) {
                Text("+")
            }
        }
    ) { padding ->

        Box(modifier = Modifier.padding(padding)) {

            if (photos.isEmpty()) {
                EmptyScreen()
            } else {
                PhotoGrid(
                    photos = photos,
                    onExport = { file ->

                        exportToGallery(context, file)

                        scope.launch {
                            snackbarHostState.showSnackbar("Фото добавлено в галерею")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun PhotoGrid(
    photos: List<File>,
    onExport: (File) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3)
    ) {
        items(photos) { file ->

            var expanded by remember { mutableStateOf(false) }

            Box {
                AsyncImage(
                    model = file,
                    contentDescription = null,
                    modifier = Modifier
                        .aspectRatio(1f)
                        .padding(2.dp),
                    contentScale = ContentScale.Crop
                )

                IconButton(
                    onClick = { expanded = true },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "menu"
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Экспорт в галерею") },
                        onClick = {
                            expanded = false
                            onExport(file)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("У вас пока нет фото")
        Spacer(modifier = Modifier.height(16.dp))
        Text("Нажмите +, чтобы сделать первое фото")
    }
}

fun createImageFile(context: Context): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    return File(storageDir, "IMG_${timeStamp}.jpg")
}

fun exportToGallery(context: Context, sourceFile: File) {

    val values = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, sourceFile.name)
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        put(
            MediaStore.Images.Media.RELATIVE_PATH,
            Environment.DIRECTORY_PICTURES + "/MyApp"
        )
        put(MediaStore.Images.Media.IS_PENDING, 1)
    }

    val uri = context.contentResolver.insert(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        values
    ) ?: return

    context.contentResolver.openOutputStream(uri).use { output ->
        sourceFile.inputStream().copyTo(output!!)
    }

    values.clear()
    values.put(MediaStore.Images.Media.IS_PENDING, 0)
    context.contentResolver.update(uri, values, null, null)
}