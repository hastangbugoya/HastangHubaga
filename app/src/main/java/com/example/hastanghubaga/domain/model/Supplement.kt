package com.example.hastanghubaga.domain.model

import com.example.hastanghubaga.data.local.entity.supplement.FrequencyType
import com.example.hastanghubaga.data.local.entity.supplement.SupplementDoseUnit
import java.time.DayOfWeek

data class Supplement(
    val id: Long,
    val name: String,
    val brand: String?,
    val notes: String?,

    val recommendedServingSize: Double,
    val recommendedDoseUnit: SupplementDoseUnit,
    val servingsPerDay: Int,
    val recommendedWithFood: Boolean?,
    val recommendedLiquidInOz: Double?,
    val recommendedTimeBetweenDailyDosesMinutes: Int?,
    val avoidCaffeine: Boolean?,

    val frequencyType: FrequencyType,
    val frequencyInterval: Int?,
    val weeklyDays: List<DayOfWeek>?,
    val offsetMinutes: Int?,

    val ingredients: List<Ingredient>,
    val isActive: Boolean
)
