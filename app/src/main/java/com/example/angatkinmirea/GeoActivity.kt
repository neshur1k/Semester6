package com.example.angatkinmirea

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*

class GeoActivity : ComponentActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setContent {
            LocationScreen(fusedLocationClient)
        }
    }
}

@Composable
fun LocationScreen(fusedLocationClient: FusedLocationProviderClient) {

    var address by remember { mutableStateOf("") }
    var coords by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }

    val context = androidx.compose.ui.platform.LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (granted) {
            getLocation(context, fusedLocationClient,
                onSuccess = { loc, addr ->
                    coords = "Lat: ${loc.latitude}, Lng: ${loc.longitude}"
                    address = addr
                    loading = false
                },
                onError = {
                    error = it
                    loading = false
                }
            )
        } else {
            error = "Разрешение не предоставлено"
            loading = false
        }
    }

    fun start() {
        error = ""
        address = ""
        coords = ""
        loading = true

        val hasPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            getLocation(context, fusedLocationClient,
                onSuccess = { loc, addr ->
                    coords = "Lat: ${loc.latitude}, Lng: ${loc.longitude}"
                    address = addr
                    loading = false
                },
                onError = {
                    error = it
                    loading = false
                }
            )
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Button(onClick = { start() }) {
            Text("Получить мой адрес")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (loading) {
            CircularProgressIndicator()
        }

        if (address.isNotEmpty()) {
            Text(text = address, style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = coords)
        }

        if (error.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = error, color = MaterialTheme.colorScheme.error)
        }
    }
}

fun getLocation(
    context: android.content.Context,
    client: FusedLocationProviderClient,
    onSuccess: (Location, String) -> Unit,
    onError: (String) -> Unit
) {
    try {
        client.lastLocation
            .addOnSuccessListener { location ->
                if (location == null) {
                    onError("Не удалось получить координаты")
                    return@addOnSuccessListener
                }

                val geocoder = Geocoder(context)
                val addresses = geocoder.getFromLocation(
                    location.latitude,
                    location.longitude,
                    1
                )

                val addressText = if (!addresses.isNullOrEmpty()) {
                    val addr = addresses[0]
                    "${addr.getAddressLine(0)}\n${addr.locality}, ${addr.countryName}"
                } else {
                    "Адрес не найден"
                }

                onSuccess(location, addressText)
            }
            .addOnFailureListener {
                onError("Ошибка получения локации: ${it.message}")
            }
    } catch (e: SecurityException) {
        onError("Нет разрешения на геолокацию")
    } catch (e: Exception) {
        onError("Ошибка: ${e.message}")
    }
}