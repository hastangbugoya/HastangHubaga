package com.example.hastanghubaga.domain.model.nutrition

import com.example.hastanghubaga.domain.model.nutrition.NutritionGoalType

data class NutritionGoal(
    val id: Long,
    val type: NutritionGoalType,
    val name: String,

    val protein: Double,
    val fat: Double,
    val carbs: Double,
    val calories: Double?,
    val sodium: Double?,
    val cholesterol: Double?,
    val fiber: Double?,

    val isActive: Boolean
)