package com.example.hastanghubaga.domain.model.nutrition

import com.example.hastanghubaga.domain.model.nutrition.NutritionGoalType
import java.time.LocalDate

data class NutritionGoal(
    val id: Long,
    val type: NutritionGoalType,
    val name: String,
    val start: Long,
    val end: Long?,
    val protein: Double,
    val fat: Double,
    val carbs: Double,
    val calories: Double?,
    val sodium: Double?,
    val cholesterol: Double?,
    val fiber: Double?,

    val isActive: Boolean
)