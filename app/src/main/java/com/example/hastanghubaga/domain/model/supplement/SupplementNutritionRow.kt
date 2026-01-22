package com.example.hastanghubaga.domain.model.supplement

data class SupplementNutritionRow(
    val logId: Long,
    val protein: Double?,
    val carbs: Double?,
    val fat: Double?,
    val calories: Double?,
    val sodium: Double?,
    val cholesterol: Double?,
    val fiber: Double?
)

