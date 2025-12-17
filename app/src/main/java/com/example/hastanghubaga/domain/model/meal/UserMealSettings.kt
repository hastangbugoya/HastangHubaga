package com.example.hastanghubaga.domain.model.meal

import java.time.LocalTime

data class UserMealSettings(
    /** Preferred default time (can override meal.defaultTime) */
    val preferredTime: LocalTime? = null,

    /** Whether reminders are enabled for this meal */
    val remindersEnabled: Boolean = false,

    /** Minutes before scheduled time to notify */
    val reminderOffsetMinutes: Int? = null,

    /** Optional portion override */
    val preferredPortionSize: Float? = null
)

