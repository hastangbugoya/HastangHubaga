package com.example.hastanghubaga.domain.schedule.model

data class SupplementSchedule(
    val supplementId: Long,
    val rule: ScheduleRule,
    val servingsPerDay: Int,
    val minSpacingMinutes: Int? = null
)
