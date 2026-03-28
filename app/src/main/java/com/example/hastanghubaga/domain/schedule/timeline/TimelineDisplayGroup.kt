package com.example.hastanghubaga.domain.schedule.timeline

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

data class TimelineDisplayGroup(
    val date: LocalDate,
    val anchorTime: LocalTime,
    val items: List<TimelineScheduledItem>
)
