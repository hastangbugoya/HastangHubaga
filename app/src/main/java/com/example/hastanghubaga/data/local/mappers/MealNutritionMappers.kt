package com.example.hastanghubaga.data.local.mappers

import com.example.hastanghubaga.domain.model.meal.MealNutrition
import com.example.hastanghubaga.domain.model.supplement.SupplementLogNutrientRow

private fun List<SupplementLogNutrientRow>.toMealNutritionFromNames(): MealNutrition {
    fun pick(name: String): Double? =
        this.firstOrNull { it.nutrientName.equals(name, ignoreCase = true) }?.amount

    // If you might have multiple entries with same name, use sum instead:
    fun sum(name: String): Double? {
        val v = this.filter { it.nutrientName.equals(name, ignoreCase = true) }.sumOf { it.amount }
        return if (v == 0.0) null else v
    }

    return MealNutrition(
        protein = sum("Protein"),
        carbs = sum("Carbs"),
        fat = sum("Fat"),
        calories = sum("Calories"),
        sodium = sum("Sodium"),
        cholesterol = sum("Cholesterol"),
        fiber = sum("Fiber")
    )
}



