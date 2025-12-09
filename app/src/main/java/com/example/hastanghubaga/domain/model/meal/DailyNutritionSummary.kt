package com.example.hastanghubaga.domain.model.meal

data class DailyNutritionSummary(
    val date: String,
    val totalProtein: Double,
    val totalFat: Double,
    val totalCarbs: Double,
    val totalCalories: Double?,

    val sodium: Double?,
    val cholesterol: Double?,
    val fiber: Double?
)