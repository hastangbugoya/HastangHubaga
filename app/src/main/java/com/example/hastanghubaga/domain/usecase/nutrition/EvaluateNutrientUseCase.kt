package com.example.hastanghubaga.domain.usecase.nutrition

import com.example.hastanghubaga.domain.model.nutrition.NutrientEvaluationResult

/**
 * Evaluates a single nutrient intake against its goal constraints.
 *
 * This is a PURE function use case.
 *
 * Why this exists:
 * - Centralizes all nutrient evaluation logic
 * - Prevents duplicated logic across plans / UI / future features
 * - Makes unit testing extremely easy
 *
 * ---
 * ## Evaluation Rules
 *
 * A nutrient may define:
 * - min (lower bound)
 * - target (informational only)
 * - max (upper bound)
 *
 * Pass conditions:
 * - If min exists → intake >= min
 * - If max exists → intake <= max
 * - If both exist → must satisfy both
 *
 * target does NOT affect pass/fail (for now)
 *
 * ---
 * ## Missing Intake
 *
 * intake == null:
 * - isMissing = true
 * - isWithinRange = false if min exists
 * - isWithinRange = true if no constraints exist
 *
 * IMPORTANT:
 * This use case does NOT decide plan success.
 * It only evaluates a single nutrient.
 *
 * ---
 * ## Future AI/dev notes
 *
 * DO NOT:
 * - duplicate this logic elsewhere
 * - assume missing == 0
 *
 * ALWAYS:
 * - call this use case for nutrient evaluation
 */
class EvaluateNutrientUseCase {

    operator fun invoke(
        nutrientKey: String,
        intake: Double?,
        min: Double?,
        target: Double?,
        max: Double?
    ): NutrientEvaluationResult {

        val isMissing = intake == null

        val isBelowMin = if (!isMissing && min != null) {
            intake!! < min
        } else {
            false
        }

        val isAboveMax = if (!isMissing && max != null) {
            intake!! > max
        } else {
            false
        }

        val isWithinRange = when {
            isMissing -> {
                // Missing intake:
                // - If no constraints → OK
                // - If min exists → fail
                // - If only max exists → OK (since no intake exceeds max)
                when {
                    min != null -> false
                    else -> true
                }
            }

            else -> {
                !isBelowMin && !isAboveMax
            }
        }

        return NutrientEvaluationResult(
            nutrientKey = nutrientKey,
            intake = intake,
            min = min,
            target = target,
            max = max,
            isWithinRange = isWithinRange,
            isBelowMin = isBelowMin,
            isAboveMax = isAboveMax,
            isMissing = isMissing
        )
    }
}