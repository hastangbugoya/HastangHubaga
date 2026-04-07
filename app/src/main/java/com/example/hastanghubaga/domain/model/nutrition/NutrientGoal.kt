package com.example.hastanghubaga.domain.model.nutrition

/**
 * Domain model for a single nutrient constraint row inside a nutrition plan.
 *
 * One row belongs to one parent [NutritionPlan].
 *
 * Notes:
 * - [nutrientKey] is HH's canonical identifier for the nutrient/ingredient dimension
 *   this goal applies to.
 * - Do not store raw AK payload naming assumptions here unless they have already
 *   been normalized into HH's canonical key space.
 * - At least one of minValue, targetValue, or maxValue should be non-null.
 *
 * Current semantics:
 * - minValue participates in effective active-plan lower-bound resolution
 * - maxValue participates in effective active-plan upper-bound resolution
 * - targetValue is advisory for now
 */
data class NutritionPlanGoal(
    val id: Long = 0L,
    val nutrientKey: String,
    val minValue: Double? = null,
    val targetValue: Double? = null,
    val maxValue: Double? = null
)