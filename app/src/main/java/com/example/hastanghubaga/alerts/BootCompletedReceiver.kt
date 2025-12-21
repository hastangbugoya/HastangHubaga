package com.example.hastanghubaga.alerts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * BroadcastReceiver invoked by the Android system after the device has
 * completed a full boot sequence.
 *
 * ## Purpose
 * This receiver exists solely to **restore time-based infrastructure**
 * that is cleared when the device reboots.
 *
 * On Android:
 * - All `AlarmManager` alarms are lost on reboot
 * - Application data (database, preferences) is preserved
 *
 * This receiver provides the entry point that allows the app to:
 * 1. Re-evaluate existing supplement schedules
 * 2. Re-register system alarms for upcoming doses
 *
 * ---
 *
 * ## Responsibilities
 *
 * - Listen for `Intent.ACTION_BOOT_COMPLETED`
 * - Start the alert scheduling pipeline (via a Service or Scheduler)
 *
 * This class **does not**:
 * - Compute schedules
 * - Access repositories directly
 * - Perform database queries
 * - Show notifications
 * - Contain business logic
 *
 * ---
 *
 * ## Architectural Role
 *
 * This receiver is part of **app-level infrastructure**, not feature code.
 * It acts as a *system → application* bridge and should remain extremely small.
 *
 * The actual scheduling logic lives in:
 * - `SupplementAlertService`
 * - `SupplementAlertScheduler`
 *
 * Keeping this receiver thin avoids:
 * - duplicate logic
 * - startup delays
 * - hard-to-debug boot-time crashes
 *
 * ---
 *
 * ## Lifecycle Notes
 *
 * - Invoked once per device boot
 * - May be delayed by the system until boot is fully complete
 * - Executes on the main thread (must return quickly)
 *
 * Any long-running or blocking work **must** be delegated to a Service.
 *
 * ---
 *
 * ## Permissions
 *
 * Requires the following permission to receive the broadcast:
 *
 * ```
 * android.permission.RECEIVE_BOOT_COMPLETED
 * ```
 *
 * Without this permission, the receiver will never be invoked.
 *
 * ---
 *
 * ## Design Constraints (Do Not Break)
 *
 * ❌ Do not perform scheduling logic here
 * ❌ Do not access DAOs or repositories
 * ❌ Do not block or perform I/O
 * ❌ Do not show UI
 *
 * ---
 *
 * ## Expected Evolution
 *
 * In the future, this receiver may also be used to:
 * - Trigger rescheduling after app updates
 * - Restore other time-based infrastructure
 *
 * Any such changes should continue to delegate work outward and
 * preserve the receiver’s role as a minimal system hook.
 */
class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Boot receivers must return quickly and should not perform any
            // scheduling or I/O themselves. All alarms are re-established by
            // delegating to the alert service, which handles work off the main thread.
            val serviceIntent = Intent(context, SupplementAlertService::class.java)
            context.startForegroundService(serviceIntent)
        }
    }
}
