package com.example.angatkinmirea

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val prefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val enabled = prefs.getBoolean("enabled", false)

        if (enabled) {
            AlarmScheduler.scheduleNext(context)
        }
    }
}