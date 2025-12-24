package com.example.hastanghubaga.alerts

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.annotation.RequiresPermission
import com.example.hastanghubaga.data.local.entity.supplement.DoseAnchorType
import com.example.hastanghubaga.domain.model.supplement.Supplement
import com.example.hastanghubaga.domain.repository.supplement.SupplementRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import javax.inject.Inject
/**
 * App-level background service responsible for scheduling system alarms
 * for upcoming supplement doses.
 *
 * ## Purpose
 * This service bridges **domain scheduling logic** and **Android system alarms**.
 * It does **not** compute schedules itself. Instead, it:
 *
 * 1. Queries the domain layer for *active supplements*
 * 2. Asks the repository for the *next absolute dose time* (`Instant`)
 * 3. Registers exact alarms with `AlarmManager`
 *
 * The service exists because:
 * - Alarm scheduling must run outside UI / feature lifecycles
 * - Alarms may need to be re-scheduled on app start, reboot, or data changes
 * - The Android framework requires a `Service` boundary for this responsibility
 *
 * ---
 *
 * ## Time Model (CRITICAL)
 * This service operates **exclusively on `kotlinx.datetime.Instant`**.
 *
 * Rationale:
 * - `Instant` represents an absolute moment on the timeline
 * - AlarmManager ultimately requires epoch milliseconds
 * - All timezone, DST, and “local date” logic is resolved *upstream*
 *
 * ❗ Do NOT introduce `LocalDate`, `LocalTime`, or `ZonedDateTime` here.
 * If formatting or calendar math is needed, it belongs in the repository
 * or domain layer — not in this service.
 *
 * ---
 *
 * ## Architectural Boundaries
 *
 * - **Domain / Repository**
 *   - Decides *if* and *when* a supplement should be taken
 *   - Returns the next dose as an `Instant`
 *
 * - **This Service**
 *   - Decides *whether to schedule* an alarm
 *   - Converts `Instant` → epoch millis
 *   - Interacts with Android system services
 *
 * - **UI / Features**
 *   - Never schedule alarms directly
 *   - Trigger rescheduling indirectly (e.g., data changes, user actions)
 *
 * This separation avoids:
 * - Duplicate scheduling logic
 * - Timezone bugs
 * - Feature-layer lifecycle leaks
 *
 * ---
 *
 * ## Alarm Semantics
 *
 * - Supplements with `DoseAnchorType.ANYTIME` are intentionally ignored
 *   (they are not time-bound and should not trigger alarms).
 *
 * - Alarms are scheduled using:
 *   - `setExactAndAllowWhileIdle`
 *   - `RTC_WAKEUP`
 *
 * This prioritizes correctness over battery optimization, which is acceptable
 * for infrequent, user-visible supplement reminders.
 *
 * ---
 *
 * ## Permissions
 *
 * On Android 12+ (API 31+), exact alarms require:
 *
 * ```
 * Manifest.permission.SCHEDULE_EXACT_ALARM
 * ```
 *
 * This service explicitly checks `canScheduleExactAlarms()` and fails silently
 * if permission is not granted. UX prompting for this permission is handled
 * elsewhere.
 *
 * ---
 *
 * ## Lifecycle Notes
 *
 * - The service is started as `START_STICKY`
 *   to allow the system to restart it if needed.
 *
 * - All work is dispatched to `Dispatchers.IO`
 *   to avoid blocking the main thread.
 *
 * ---
 *
 * ## Things Future You Should NOT Do
 *
 * ❌ Do not re-introduce `java.time` types here
 * ❌ Do not move scheduling logic into feature modules
 * ❌ Do not compute next-dose logic in this service
 * ❌ Do not format dates or times here
 *
 * ---
 *
 * ## Expected Evolution
 *
 * In the future, this service may:
 * - Be triggered by BOOT_COMPLETED receivers
 * - Respond to data sync / cloud updates
 * - Be extracted into a dedicated scheduler + thin service wrapper
 *
 * Those changes should **not** alter the Instant-based contract.
 */
@AndroidEntryPoint
class SupplementAlertService : Service() {

    @Inject lateinit var repo: SupplementRepository

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        CoroutineScope(Dispatchers.IO).launch {
            scheduleActiveSupplementAlerts()
        }
        return START_STICKY
    }

    private suspend fun scheduleActiveSupplementAlerts() {
        val context = applicationContext

        val active = repo.getActiveSupplements().first()

        active.forEach { supplement ->
            if (supplement.doseAnchorType == DoseAnchorType.ANYTIME) return@forEach

            val nextDose: Instant = repo.getNextDoseDateTime(supplement) ?: return@forEach

            scheduleAlarmForSupplement(context, supplement, nextDose)
        }
    }

    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    private fun scheduleAlarmForSupplement(
        context: Context,
        supplement: Supplement,
        instant: Instant
    ) {
        val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return

        val canSchedule =
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S)
                alarmManager.canScheduleExactAlarms()
            else true

        if (!canSchedule) return

        val triggerMillis = instant.toEpochMilliseconds()

        val intent = Intent(context, SupplementAlertReceiver::class.java).apply {
            putExtra("supplementId", supplement.id)
            putExtra("supplementName", supplement.name)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            supplement.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerMillis,
            pendingIntent
        )
    }
}


