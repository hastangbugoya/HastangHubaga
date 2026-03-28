package com.example.hastanghubaga.domain.schedule.model

import kotlinx.datetime.LocalTime

data class ResolvedScheduleTime(
    val time: LocalTime,
    val label: String? = null,
    val sortOrderHint: Int? = null
)
