package com.example.hastanghubaga.domain.model.nutrition
/**
 * Aggregated nutrition totals for a single day.
 *
 * Provides separated nutrition totals by source as well as the combined
 * total. Intended as a stable result model for use cases and UI layers.
 */
data class DailyNutritionTotals(
    val meals: Nutrition,
    val supplements: Nutrition,
    val total: Nutrition
)
