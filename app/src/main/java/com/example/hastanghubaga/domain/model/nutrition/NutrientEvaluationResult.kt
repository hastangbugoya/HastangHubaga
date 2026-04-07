package com.example.hastanghubaga.domain.model.nutrition

/**
 * Represents the evaluation result of a SINGLE nutrient against its goal constraints
 * for a specific day.
 *
 * This is the lowest-level unit of the compliance engine.
 * Everything (plan success, day success, UI display) builds on top of this.
 *
 * Why this exists:
 * - We need a consistent, testable representation of "did this nutrient pass?"
 * - UI should not re-derive logic like min/max checks
 * - Supports future extensions (warnings, scoring, severity levels)
 *
 * ---
 * ## Evaluation semantics
 *
 * A nutrient may define:
 * - min (lower bound)
 * - target (informational, optional)
 * - max (upper bound)
 *
 * A nutrient is considered "passing" if:
 *
 * - If min exists → intake >= min
 * - If max exists → intake <= max
 * - If both exist → must satisfy both
 *
 * target does NOT affect pass/fail (display only, for now)
 *
 * ---
 * ## Missing intake behavior
 *
 * - If intake == null → nutrient is considered "missing"
 * - Missing may be treated as failure IF a min constraint exists
 * - The compliance engine decides final interpretation
 *
 * ---
 * ## Flags
 *
 * These are explicitly broken out to avoid re-computation in UI or higher layers.
 *
 * ---
 * ## Future AI/dev notes
 *
 * DO NOT:
 * - recompute pass/fail logic elsewhere
 * - assume missing == 0
 *
 * ALWAYS:
 * - use these flags as the source of truth for evaluation
 */
data class NutrientEvaluationResult(

    val nutrientKey: String,

    val intake: Double?,

    val min: Double?,

    val target: Double?,

    val max: Double?,

    /**
     * True if intake satisfies all defined constraints.
     */
    val isWithinRange: Boolean,

    /**
     * True if intake is below min (only meaningful if min != null and intake != null).
     */
    val isBelowMin: Boolean,

    /**
     * True if intake is above max (only meaningful if max != null and intake != null).
     */
    val isAboveMax: Boolean,

    /**
     * True if intake was not provided.
     */
    val isMissing: Boolean
)