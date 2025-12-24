package com.example.hastanghubaga.alerts

import com.example.hastanghubaga.domain.model.timeline.UpcomingSchedule
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

class SupplementAlertScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {

    // ✅ AlarmManager belongs HERE (single owner, app context)
    private val alarmManager: AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    private val zoneId: ZoneId = ZoneId.systemDefault()

    /* ---------------------------------------------------------------------- */
    /* Capability                                                             */
    /* ---------------------------------------------------------------------- */

    /**
     * Whether this app is currently allowed to schedule exact alarms.
     *
     * UI layer should observe this and prompt the user if false.
     */
    fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    /* ---------------------------------------------------------------------- */
    /* Scheduling API                                                          */
    /* ---------------------------------------------------------------------- */

    fun reschedule(items: List<UpcomingSchedule>) {
        val nowMillis = System.currentTimeMillis()

        items
            .asSequence()
            .filter { !it.isCompleted }
            .map { item ->
                val triggerAtMillis =
                    item.scheduledAt
                        .toInstant(TimeZone.currentSystemDefault())
                        .toEpochMilliseconds()

                item to triggerAtMillis
            }
            .filter { (_, triggerAtMillis) ->
                triggerAtMillis > nowMillis
            }
            .distinctBy { (item, _) -> item.id }
            .forEach { (item, triggerAtMillis) ->
                scheduleOne(item, triggerAtMillis)
            }
    }

    private fun scheduleOne(
        item: UpcomingSchedule,
        triggerAtMillis: Long
    ) {
        val pendingIntent = buildPendingIntent(item)

        when (scheduleExact(triggerAtMillis, pendingIntent)) {
            ScheduleResult.Scheduled -> {
                // success — nothing else to do
            }
            ScheduleResult.PermissionRequired -> {
                // intentionally no-op
                // UI will observe capability and prompt user
            }
        }
    }

    /**
     * Schedules an exact alarm if permitted.
     * Never throws.
     */
    private fun scheduleExact(
        triggerAtMillis: Long,
        pendingIntent: PendingIntent
    ): ScheduleResult {
        if (!canScheduleExactAlarms()) {
            return ScheduleResult.PermissionRequired
        }

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )

        return ScheduleResult.Scheduled
    }

    /* ---------------------------------------------------------------------- */
    /* PendingIntent                                                           */
    /* ---------------------------------------------------------------------- */

    private fun buildPendingIntent(
        item: UpcomingSchedule
    ): PendingIntent {
        val intent = SupplementAlertReceiver.createIntent(
            context = context,
            schedule = item
        )

        return PendingIntent.getBroadcast(
            context,
            item.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
