package com.example.hastanghubaga.domain.model.nutrition
/**
 * Represents the compliance result for a SINGLE nutrition plan on a SINGLE day.
 *
 * This model is the bridge between:
 * - low-level nutrient evaluation
 * - day-level success aggregation
 *
 * Why this exists:
 * - successMode is defined per plan, not globally
 * - each plan may evaluate a different set of nutrients
 * - the app should be able to explain WHY a plan passed or failed
 *
 * A plan may:
 * - track many nutrients
 * - only count some toward success
 * - count none toward success (tracking-only mode)
 *
 * This result preserves both:
 * - the nutrients that were evaluated for display
 * - the subset that actually counted toward success
 *
 * ---
 * ## successMode reminders
 *
 * ALL_TRACKED_GOALS
 * - all evaluated nutrients must pass
 *
 * ANY_TRACKED_GOAL
 * - at least one evaluated nutrient must pass
 *
 * CUSTOM_SELECTED_GOALS
 * - only nutrients chosen in nutrition_plan_success_criteria count toward success
 * - if no nutrients are selected, plan is intentionally successful
 *
 * NONE
 * - plan is always successful
 * - nutrient comparisons may still be shown for tracking/detail purposes
 *
 * ---
 * ## Future AI/dev notes
 *
 * DO NOT:
 * - assume all tracked nutrients counted toward success
 * - infer success from nutrient rows alone
 *
 * ALWAYS:
 * - respect [successMode]
 * - use [evaluatedNutrientKeys] to know what counted toward success
 * - use [allNutrientResults] for full detail rendering
 */
data class PlanComplianceResult(
    val planId: Long,
    val planName: String,
    val successMode: String,
    val isSuccessful: Boolean,

    /**
     * Nutrient keys that actually counted toward success for this plan.
     *
     * Examples:
     * - ALL / ANY -> usually all tracked nutrient keys in the plan
     * - CUSTOM -> only selected success-criteria keys
     * - NONE -> typically empty
     */
    val evaluatedNutrientKeys: Set<String>,

    /**
     * Full nutrient comparison results available for this plan.
     *
     * This may include nutrients that were tracked for display but did not count
     * toward success, depending on future UI/domain decisions.
     */
    val allNutrientResults: List<NutrientEvaluationResult>
)