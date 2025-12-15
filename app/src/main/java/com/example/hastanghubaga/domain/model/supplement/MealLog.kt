package com.example.hastanghubaga.domain.model.supplement

import com.example.hastanghubaga.data.local.entity.meal.MealType
import java.time.LocalTime

data class MealLog(
    val mealType: MealType,
    val time: LocalTime,
    val wasSkipped: Boolean = false
)