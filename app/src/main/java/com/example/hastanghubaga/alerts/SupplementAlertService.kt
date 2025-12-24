package com.example.hastanghubaga.alerts

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.hastanghubaga.R

class SupplementAlertService : Service() {

    override fun onCreate() {
        super.onCreate()
        ensureChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val title = intent?.getStringExtra(SupplementAlertReceiver.EXTRA_TITLE)
            ?: "Supplement Reminder"
        val subtitle = intent?.getStringExtra(SupplementAlertReceiver.EXTRA_SUBTITLE)

        startForeground(NOTIFICATION_ID, buildNotification(title, subtitle))

        // If you already have your own sound/vibration logic, keep it.
        // Ideally: let the notification channel handle sound/vibration for watch forwarding.

        stopSelf()
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification(title: String, subtitle: String?): Notification {
        val text = subtitle?.takeIf { it.isNotBlank() } ?: "Tap to view"

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_supplement_eye_alert) // adjust to your icon
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .build()
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val mgr = getSystemService(NotificationManager::class.java)

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Supplement Alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Time-sensitive supplement reminders"

            // Optional: set sound at channel level (recommended for consistent behavior + watch forwarding)
            val sound: Uri? = null // keep null if you set sound elsewhere, otherwise set one
            if (sound != null) {
                val attrs = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
                setSound(sound, attrs)
            }
        }

        mgr.createNotificationChannel(channel)
    }

    companion object {
        private const val CHANNEL_ID = "supplement_alerts"
        private const val NOTIFICATION_ID = 1001
    }
}
