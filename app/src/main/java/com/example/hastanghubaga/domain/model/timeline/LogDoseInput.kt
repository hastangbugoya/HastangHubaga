package com.example.hastanghubaga.domain.model.timeline

import com.example.hastanghubaga.data.local.entity.supplement.SupplementDoseUnit
import com.example.hastanghubaga.domain.time.TimeUseIntent
import kotlinx.datetime.LocalTime

data class LogDoseInput(
    val supplementId: Long,
    val fractionTaken: Double,
    val unit: SupplementDoseUnit,
    val timeUseIntent: TimeUseIntent,
    val occurrenceId: String? = null,
    val plannedTime: LocalTime? = null
)

