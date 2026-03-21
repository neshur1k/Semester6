package com.example.angatkinmirea
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CurrencyViewModel : ViewModel() {
    private val _rate = MutableStateFlow(90.0)
    val rate: StateFlow<Double> = _rate

    private val _lastUpdate = MutableStateFlow("")
    val lastUpdate: StateFlow<String> = _lastUpdate

    private val _trend = MutableStateFlow(0)
    val trend: StateFlow<Int> = _trend
    private var previousRate = 90.0

    init {
        generateRate()
        viewModelScope.launch {
            while (true) {
                delay(5000)
                generateRate()
            }
        }
    }

    fun generateRate() {
        val newRate = 90 + Random.nextDouble(-2.0, 2.0)
        _rate.value = newRate

        _trend.value = when {
            newRate > previousRate -> 1
            newRate < previousRate -> -1
            else -> _trend.value
        }

        val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        _lastUpdate.value = time
    }
}