package com.example.hastanghubaga.domain.model.meal

data class MealNutrition(
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val calories: Double?,
    val sodium: Double?,
    val cholesterol: Double?,
    val fiber: Double?
)