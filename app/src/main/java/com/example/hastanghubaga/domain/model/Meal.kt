package com.example.hastanghubaga.domain.model

import java.time.LocalDateTime

data class Meal(
    val id: Long,
    val type: MealType,
    val timestamp: LocalDateTime,
    val nutrition: MealNutrition,
    val notes: String? = null
)
