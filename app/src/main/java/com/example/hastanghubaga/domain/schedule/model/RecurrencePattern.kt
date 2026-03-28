package com.example.hastanghubaga.domain.schedule.model

import kotlinx.datetime.DayOfWeek

sealed interface RecurrencePattern {
    data class Daily(
        val intervalDays: Int = 1
    ) : RecurrencePattern

    data class Weekly(
        val intervalWeeks: Int = 1,
        val daysOfWeek: Set<DayOfWeek>
    ) : RecurrencePattern
}