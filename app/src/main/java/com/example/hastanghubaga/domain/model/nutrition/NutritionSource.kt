package com.example.hastanghubaga.domain.model.nutrition
/**
 * Identifies the originating category of a nutrition contribution.
 *
 * Used for attribution and aggregation (e.g., separating meal-based
 * nutrition from supplement-based nutrition) without polluting the
 * Nutrition model itself.
 */
enum class NutritionSource {
    MEAL,
    SUPPLEMENT
}