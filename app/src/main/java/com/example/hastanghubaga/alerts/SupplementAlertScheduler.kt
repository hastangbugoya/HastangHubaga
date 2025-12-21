package com.example.hastanghubaga.alerts

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.hastanghubaga.data.local.entity.supplement.DoseAnchorType
import com.example.hastanghubaga.domain.model.supplement.Supplement
import com.example.hastanghubaga.domain.repository.supplement.SupplementRepository
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant
import kotlinx.datetime.toKotlinInstant
import java.time.ZonedDateTime

class SupplementAlertScheduler(
    private val repo: SupplementRepository
) {

    fun scheduleUpcomingDoses(context: Context) {

        val now = ZonedDateTime.now().toInstant().toKotlinInstant()
        val cutoff = ZonedDateTime.now().plusDays(1).toInstant().toKotlinInstant()// schedule next 24 hours

        runBlocking {

            repo.getAllSupplementsOnce()
                .filter { repo.isActive(it) }
                .forEach { supplement ->

                    if (supplement.doseAnchorType == DoseAnchorType.ANYTIME) {
                        return@forEach
                    }

                    val nextDose = repo.getNextDoseDateTime(supplement)

                    if (nextDose != null && nextDose >= now && nextDose < cutoff) {
                        scheduleAlarm(context, supplement, nextDose)
                    }
                }
        }
    }


    private fun scheduleAlarm(
        context: Context,
        supplement: Supplement,
        dateTime: Instant
    ) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
            ?: return

        val triggerMillis = dateTime.toEpochMilliseconds()

        val intent = Intent(context, SupplementAlertReceiver::class.java).apply {
            putExtra("supplement_id", supplement.id)
            putExtra("supplement_name", supplement.name)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            supplement.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Android S+ exact alarm permission check
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S &&
            !alarmManager.canScheduleExactAlarms()
        ) {
            return
        }

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerMillis,
            pendingIntent
        )
    }
}
