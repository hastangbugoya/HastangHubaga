package com.example.hastanghubaga.alerts

import android.Manifest
import android.R
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import com.example.hastanghubaga.data.local.mappers.toLocalTimeLong
import com.example.hastanghubaga.domain.model.supplement.Supplement
import com.example.hastanghubaga.domain.repository.supplement.SupplementRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject


class SupplementAlertService : Service() {

    @Inject lateinit var repo: SupplementRepository
    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        CoroutineScope(Dispatchers.IO).launch {
            scheduleActiveSupplementAlerts()
        }
        return START_STICKY
    }

    private suspend fun scheduleActiveSupplementAlerts() {
        val today = LocalDate.now()

        val active = repo.getActiveSupplements().first()

        val dueToday = active.filter { repo.shouldTakeToday(it, today) }

        val context = applicationContext

        dueToday.forEach { supp ->
            val time = repo.getPredictedNextDoseTime(supp, today)
            if (time != null) {
                scheduleAlarmForSupplement(context, supp, time)
            }
        }
    }

    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    private fun scheduleAlarmForSupplement(
        context: Context,
        supp: Supplement,
        localTime: LocalTime
    ) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)

        val canSchedule = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true // Before Android 12, exact alarms are always allowed
        }

        if (!canSchedule) {
            // OPTIONAL: You can notify user or open settings
            // val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            // context.startActivity(intent)
            return
        }

        val triggerMillis = localTime
            .atDate(LocalDate.now())
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        val intent = Intent(context, SupplementAlertReceiver::class.java).apply {
            putExtra("supplementId", supp.id)
            putExtra("supplementName", supp.name)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            supp.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerMillis,
                pendingIntent
            )
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

}

