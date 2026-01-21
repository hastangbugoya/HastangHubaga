package com.example.hastanghubaga.domain.model.activity

import androidx.room.ColumnInfo
import com.example.hastanghubaga.domain.model.activity.ActivityType
import kotlinx.datetime.LocalDateTime


data class Activity(
    val id: Long,
    val type: ActivityType,
    val start: LocalDateTime,
    val end: LocalDateTime?,
    val notes: String? = null,
    val intensity: Int? = null,
    val sendAlert: Boolean = false,
    val alertOffsetMinutes: Int? = null
)