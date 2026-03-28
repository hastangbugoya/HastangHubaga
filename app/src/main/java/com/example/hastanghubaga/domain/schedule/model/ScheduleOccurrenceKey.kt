package com.example.hastanghubaga.domain.schedule.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

data class ScheduleOccurrenceKey(
    val date: LocalDate,
    val time: LocalTime
)
