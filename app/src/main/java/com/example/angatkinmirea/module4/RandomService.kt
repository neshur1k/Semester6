package com.example.angatkinmirea.module4
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RandomService : Service() {

    private val binder = LocalBinder()
    private var isRunning = false
    private var currentNumber = 0

    private val scope = CoroutineScope(Dispatchers.Default)

    inner class LocalBinder : Binder() {
        fun getService(): RandomService = this@RandomService
    }

    override fun onBind(intent: Intent): IBinder {
        startGenerating()
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        stopGenerating()
        return super.onUnbind(intent)
    }

    private fun startGenerating() {
        isRunning = true

        scope.launch {
            while (isRunning) {
                currentNumber = (0..100).random()
                delay(1000)
            }
        }
    }

    private fun stopGenerating() {
        isRunning = false
    }

    fun getNumber(): Int = currentNumber
}