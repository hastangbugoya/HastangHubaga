package com.example.hastanghubaga.domain.model.timeline

import com.example.hastanghubaga.ui.timeline.TodayUiRowType
import kotlinx.datetime.LocalDateTime

data class UpcomingSchedule(
    val id: Long = 0,
    val type: TodayUiRowType,
    val referenceId: Long,
    val scheduledAt: LocalDateTime,
    val title: String,
    val subtitle: String?,
    val isCompleted: Boolean = false
)


