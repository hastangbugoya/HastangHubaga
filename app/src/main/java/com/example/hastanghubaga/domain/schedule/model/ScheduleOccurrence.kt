package com.example.hastanghubaga.domain.schedule.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

data class ScheduleOccurrence(
    val date: LocalDate,
    val time: LocalTime,
    val label: String? = null,
    val key: ScheduleOccurrenceKey
)
