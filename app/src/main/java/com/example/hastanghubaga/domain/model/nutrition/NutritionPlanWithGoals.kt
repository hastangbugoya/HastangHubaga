package com.example.hastanghubaga.domain.model.nutrition

/**
 * Aggregated domain model representing a nutrition plan together with its
 * child nutrient goal rows.
 *
 * This is the primary shape used by higher layers when they need a complete
 * plan definition instead of just parent metadata.
 *
 * Design:
 * - [plan] = parent metadata such as name, type, active state, and source
 * - [goals] = normalized child rows, one per nutrientKey
 *
 * This stays intentionally passive:
 * - no effective-range resolution
 * - no conflict detection
 * - no import-comparison behavior
 *
 * Those behaviors should live in dedicated use cases above the repository layer.
 */
data class NutritionPlanWithGoals(
    val plan: NutritionPlan,
    val goals: List<NutritionPlanGoal>
)
