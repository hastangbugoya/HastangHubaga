package com.example.hastanghubaga.domain.model.activity

import com.example.hastanghubaga.domain.model.activity.ActivityType
import kotlinx.datetime.LocalDateTime


data class Activity(
    val id: Long,
    val type: ActivityType,
    val start: LocalDateTime,
    val end: LocalDateTime?,
    val notes: String? = null,
    val intensity: Int? = null
)