package com.example.hastanghubaga.domain.model

import java.time.LocalDateTime

data class Activity(
    val id: Long,
    val type: ActivityType,
    val start: LocalDateTime,
    val end: LocalDateTime?,
    val notes: String? = null
)
