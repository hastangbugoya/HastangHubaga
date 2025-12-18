package com.example.hastanghubaga.domain.model.nutrition
/**
 * Associates a Nutrition instance with its originating source.
 *
 * This wrapper is used during aggregation and reporting to enable
 * breakdowns by source (e.g., meals vs supplements) while keeping
 * Nutrition itself free of contextual metadata.
 */
data class SourcedNutrition(
    val source: NutritionSource,
    val nutrition: Nutrition
)
