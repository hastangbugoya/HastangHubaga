package com.example.hastanghubaga.domain.model.meal

import com.example.hastanghubaga.data.local.entity.meal.MealType

data class MealWithNutrition(
    val id: Long,
    val type: MealType,
    val timestamp: Long,
    val notes: String?,

    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val calories: Double?,
    val sodium: Double?,
    val cholesterol: Double?,
    val fiber: Double?
)
