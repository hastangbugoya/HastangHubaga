package com.example.hastanghubaga.domain.schedule.timeline

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

data class TimelineScheduledItem(
    val sourceType: TimelineSourceType,
    val sourceId: Long,
    val scheduleRuleId: Long,
    val date: LocalDate,
    val time: LocalTime,
    val title: String
)
