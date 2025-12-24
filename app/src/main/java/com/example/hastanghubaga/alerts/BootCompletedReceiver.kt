package com.example.hastanghubaga.alerts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != "android.intent.action.BOOT_COMPLETED") return

        val req = OneTimeWorkRequestBuilder<RescheduleUpcomingAlertsWorker>().build()
        WorkManager.getInstance(context).enqueue(req)
    }
}
