package com.example.hastanghubaga.domain.model.nutrition

data class MealNutritionAtTimeRow(
    val timestamp: Long,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val calories: Int,
    val sodium: Double?,
    val cholesterol: Double?,
    val fiber: Double?
)
