package com.example.hastanghubaga.domain.model.nutrition
/**
 * Represents nutritional values for a single consumable item.
 *
 * This model is intentionally source-agnostic and free of scheduling,
 * UI, or persistence concerns. It may describe nutrition for a meal,
 * a supplement, or any other consumable.
 *
 * All fields are nullable to support partial or unknown nutrition data.
 * Aggregation logic is handled elsewhere.
 */
data class Nutrition(
    val calories: Int?,
    val proteinGrams: Double?,
    val carbsGrams: Double?,
    val fatGrams: Double?,
    val fiberGrams: Double? = null
)
