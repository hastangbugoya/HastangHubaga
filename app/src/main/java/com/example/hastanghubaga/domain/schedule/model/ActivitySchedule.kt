package com.example.hastanghubaga.domain.schedule.model

data class ActivitySchedule(
    val activityTemplateId: Long,
    val rule: ScheduleRule,
    val defaultDurationMinutes: Int? = null
)
