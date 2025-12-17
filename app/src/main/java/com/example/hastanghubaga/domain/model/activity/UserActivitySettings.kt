package com.example.hastanghubaga.domain.model.activity

import java.time.LocalTime

data class UserActivitySettings(
    /** Preferred time of day for this activity */
    val preferredTime: LocalTime? = null,

    /** Preferred duration override (minutes) */
    val preferredDurationMinutes: Int? = null,

    /** Whether reminders are enabled */
    val remindersEnabled: Boolean = false,

    /** Reminder offset in minutes */
    val reminderOffsetMinutes: Int? = null
)
