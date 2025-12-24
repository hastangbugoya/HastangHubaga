package com.example.hastanghubaga

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.hastanghubaga.alerts.UpcomingAlertSchedulingOrchestrator
import com.example.hastanghubaga.widget.UpdateSupplementWidgetWorker
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class HastangHubagaApp() : Application(), Configuration.Provider {
    @Inject
    lateinit var orchestrator: UpcomingAlertSchedulingOrchestrator
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() {
            Log.d("Meow","HastangHubagaApp > HiltWorkerFactory > workerFactory: $workerFactory")
            return Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
        }

    override fun onCreate() {
        super.onCreate()

//        val channel = NotificationChannel(
//            "supplement_alerts",
//            "Supplement Alerts",
//            NotificationManager.IMPORTANCE_HIGH
//        ).apply {
//            description = "Reminders to take your supplements"
//        }

        orchestrator.start(CoroutineScope(Dispatchers.Default))

//        val manager = getSystemService(NotificationManager::class.java)
//        manager.createNotificationChannel(channel)

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