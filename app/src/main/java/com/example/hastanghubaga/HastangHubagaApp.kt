package com.example.hastanghubaga

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.hastanghubaga.widget.UpdateSupplementWidgetWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit

@HiltAndroidApp
class HastangHubagaApp : Application(){

    override fun onCreate() {
        super.onCreate()

        val channel = NotificationChannel(
            "supplement_alerts",
            "Supplement Alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Reminders to take your supplements"
        }

        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)

        scheduleWidgetWorker()
    }

    private fun scheduleWidgetWorker() {
        val request = PeriodicWorkRequestBuilder<UpdateSupplementWidgetWorker>(
            12, TimeUnit.HOURS
        ).build()

        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork(
                "update_supp_widget",
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
    }
}