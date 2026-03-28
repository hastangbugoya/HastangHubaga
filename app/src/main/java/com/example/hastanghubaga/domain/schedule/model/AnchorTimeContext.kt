package com.example.hastanghubaga.domain.schedule.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

data class AnchorTimeContext(
    val date: LocalDate,
    val defaultTimes: Map<TimeAnchor, LocalTime>,
    val dayOfWeekOverrides: Map<AnchorDayKey, LocalTime> = emptyMap(),
    val dateOverrides: Map<AnchorDateKey, LocalTime> = emptyMap()
)
