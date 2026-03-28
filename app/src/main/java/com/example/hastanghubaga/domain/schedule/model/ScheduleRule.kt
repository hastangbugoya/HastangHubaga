package com.example.hastanghubaga.domain.schedule.model

data class ScheduleRule(
    val recurrence: RecurrencePattern,
    val timing: ScheduleTiming,
    val window: RecurrenceWindow,
    val isEnabled: Boolean = true
)
