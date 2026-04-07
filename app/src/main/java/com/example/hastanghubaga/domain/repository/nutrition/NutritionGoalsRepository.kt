package com.example.hastanghubaga.domain.repository.nutrition

/**
 * Legacy compatibility alias.
 *
 * The old nutrition-goals repository contract was based on the obsolete
 * single-row goal model. HH now uses a normalized multi-plan architecture
 * through [NutritionPlanRepository].
 *
 * Keep this alias temporarily so older references can be migrated gradually
 * without preserving the wrong repository shape.
 *
 * Future cleanup:
 * - Remove this file after all callers have been migrated to
 *   [NutritionPlanRepository].
 */
@Deprecated(
    message = "Replaced by NutritionPlanRepository in the normalized multi-plan nutrition architecture.",
    replaceWith = ReplaceWith("NutritionPlanRepository")
)
typealias NutritionGoalsRepository = NutritionPlanRepository