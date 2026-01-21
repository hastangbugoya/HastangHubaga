package com.example.hastanghubaga.domain.model.meal

import com.example.hastanghubaga.data.local.entity.meal.MealType
import kotlinx.datetime.LocalDateTime


data class Meal(
    val id: Long,
    val type: MealType,
    val timestamp: LocalDateTime,
    val nutrition: MealNutrition?,
    val notes: String? = null,
    val sendAlert: Boolean = false,
    val alertOffsetMinutes: Int? = null
)