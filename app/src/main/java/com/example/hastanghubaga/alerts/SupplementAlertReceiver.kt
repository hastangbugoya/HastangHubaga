package com.example.hastanghubaga.alerts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.example.hastanghubaga.domain.model.timeline.UpcomingSchedule

class SupplementAlertReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val serviceIntent = Intent(context, SupplementAlertService::class.java).apply {
            putExtras(intent.extras ?: return)
        }
        ContextCompat.startForegroundService(context, serviceIntent)
    }

    companion object {
        const val EXTRA_ID = "extra_schedule_id"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_SUBTITLE = "extra_subtitle"
        const val EXTRA_REFERENCE_ID = "extra_reference_id"
        const val EXTRA_TYPE = "extra_type"
        fun createIntent(
            context: Context,
            schedule: UpcomingSchedule
        ): Intent =
            Intent(context, SupplementAlertReceiver::class.java).apply {
                putExtra(EXTRA_ID, schedule.id)
                putExtra(EXTRA_TITLE, schedule.title)
                putExtra(EXTRA_SUBTITLE, schedule.subtitle)
                putExtra(EXTRA_REFERENCE_ID, schedule.referenceId)
                putExtra(EXTRA_TYPE, schedule.type.name)
            }
    }
}
