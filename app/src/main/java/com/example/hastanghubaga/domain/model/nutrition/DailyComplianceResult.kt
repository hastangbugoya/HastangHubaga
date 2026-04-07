package com.example.hastanghubaga.domain.model.nutrition

/**
 * Represents the final daily nutrition compliance output for a single date.
 *
 * This is the top-level result returned by the HH daily compliance engine.
 *
 * It answers:
 * - which plans applied to the day
 * - how each plan evaluated
 * - whether the day was successful overall
 * - what nutrient-level comparisons were produced
 *
 * Why this exists:
 * - calendar/day UI should consume a single structured result
 * - domain logic should decide success before UI rendering
 * - future monthly compliance can compose from daily results instead of
 *   re-implementing nutrient evaluation rules
 *
 * ---
 * ## Day-level success semantics
 *
 * Recommended initial rule:
 * - if there are no applicable plans, the day is successful
 * - if there are applicable plans, the day is successful only if all applicable
 *   plans are successful
 *
 * This keeps day-level behavior strict, explicit, and easy to reason about.
 * If needed later, this aggregation strategy can become configurable without
 * changing the lower-level evaluation models.
 *
 * ---
 * ## allNutrientResults
 *
 * This is the day-level rolled-up nutrient result set intended for:
 * - detail display
 * - diagnostics
 * - future month/calendar summaries
 *
 * It does NOT replace the per-plan results.
 * Per-plan results remain the source of truth for plan-specific success logic.
 *
 * ---
 * ## Future AI/dev notes
 *
 * DO NOT:
 * - bypass plan-level results when determining success
 * - treat this as a UI-only model
 *
 * ALWAYS:
 * - compute day success from planResults
 * - keep this model stable enough for future monthly reuse
 */
data class DailyComplianceResult(
    val date: Long,
    val isSuccessful: Boolean,
    val planResults: List<PlanComplianceResult>,
    val allNutrientResults: List<NutrientEvaluationResult>
)
