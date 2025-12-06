package com.example.hastanghubaga.alerts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

class SupplementAlertReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        val name = intent.getStringExtra("supplement_name") ?: "Your supplement"

        val serviceIntent = Intent(context, SupplementAlertService::class.java).apply {
            putExtras(intent.extras!!)
        }

        ContextCompat.startForegroundService(context, serviceIntent)
    }
}
