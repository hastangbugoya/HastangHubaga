package com.example.hastanghubaga.domain.model.meal

import com.example.hastanghubaga.data.local.entity.meal.MealType
import com.example.hastanghubaga.domain.time.TimeUseIntent

data class LogMealInput(
    val occurrenceId: String? = null,
    val mealType: MealType,
    val timeUseIntent: TimeUseIntent,
    val notes: String? = null,

    // Nutrition is optional. If null, only MealEntity is saved.
    val nutrition: NutritionInput? = null
)

data class NutritionInput(
    val calories: Int? = null,
    val proteinGrams: Double? = null,
    val carbsGrams: Double? = null,
    val fatGrams: Double? = null,
    val sodiumMg: Double? = null,
    val cholesterolMg: Double? = null,
    val fiberGrams: Double? = null
)