package com.example.hastanghubaga.domain.model.supplement

import androidx.room.PrimaryKey
import com.example.hastanghubaga.data.local.entity.supplement.SupplementDoseUnit

data class SupplementDailyLog(
    val supplementId: Long,
    val date: String,
    val actualServingTaken: Double,
    val doseUnit: SupplementDoseUnit,
    val timestamp: Long
)