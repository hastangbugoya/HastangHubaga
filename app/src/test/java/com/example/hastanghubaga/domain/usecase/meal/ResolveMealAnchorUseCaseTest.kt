package com.example.hastanghubaga.domain.usecase.meal

import com.example.hastanghubaga.data.local.entity.meal.MealType
import com.example.hastanghubaga.domain.model.meal.Meal
import com.example.hastanghubaga.domain.schedule.model.TimeAnchor
import javax.inject.Inject

/**
 * ============================================================
 * ResolveMealAnchorUseCase
 * ============================================================
 * 📅 Created: 2026-04-04
 *
 * PURPOSE (Human Developer Guide)
 * ------------------------------------------------------------
 * This use case determines whether a Meal can act as a scheduling anchor.
 *
 * Meals in the current architecture are:
 * - Templates (NOT timestamped events)
 * - Potential anchor providers for scheduling systems
 *
 * This use case converts a Meal into a TimeAnchor (if applicable).
 *
 * ------------------------------------------------------------
 * RULES
 * ------------------------------------------------------------
 * 1. treatAsAnchor overrides everything
 * 2. If no override:
 *    - BREAKFAST → TimeAnchor.BREAKFAST
 *    - LUNCH     → TimeAnchor.LUNCH
 *    - DINNER    → TimeAnchor.DINNER
 * 3. All other types → NOT anchors → return null
 *
 * ------------------------------------------------------------
 * NON-GOALS (IMPORTANT)
 * ------------------------------------------------------------
 * This use case DOES NOT:
 * - Assign timestamps
 * - Determine actual occurrence times
 * - Affect timeline rendering
 * - Perform scheduling
 *
 * It is intentionally PURE and SIDE-EFFECT FREE.
 *
 * ------------------------------------------------------------
 * WHY THIS EXISTS
 * ------------------------------------------------------------
 * We are transitioning from:
 *   Meal (timestamped event)
 * → Meal (template) + Schedule + Occurrence
 *
 * This use case allows:
 * - Supplements to anchor to meals
 * - Activities to anchor to meals
 * - Future meal scheduling system to resolve anchors consistently
 *
 * ------------------------------------------------------------
 * DESIGN PRINCIPLE
 * ------------------------------------------------------------
 * Keep anchor resolution:
 * - deterministic
 * - minimal
 * - decoupled from time
 *
 * Time resolution happens later in:
 * - ResolveAnchorTimeUseCase
 * - Planned occurrence builders
 *
 */
class ResolveMealAnchorUseCase @Inject constructor() {

    operator fun invoke(meal: Meal): TimeAnchor? {
        // 1. Explicit override
        meal.treatAsAnchor?.let { overrideType ->
            return mapMealTypeToAnchor(overrideType)
        }

        // 2. Fallback to actual type
        return mapMealTypeToAnchor(meal.type)
    }

    private fun mapMealTypeToAnchor(type: MealType): TimeAnchor? {
        return when (type) {
            MealType.BREAKFAST -> TimeAnchor.BREAKFAST
            MealType.LUNCH -> TimeAnchor.LUNCH
            MealType.DINNER -> TimeAnchor.DINNER
            else -> null
        }
    }
}

/**
 * ============================================================
 * ⚠️ AI ASSISTANT / FUTURE AGENT KDoc
 * ============================================================
 * 📅 Created: 2026-04-04
 *
 * READ THIS BEFORE MODIFYING
 * ------------------------------------------------------------
 *
 * This use case is part of the core scheduling contract.
 *
 * DO NOT:
 * ❌ Add time logic here
 * ❌ Read timestamps (Meal has NO timestamp)
 * ❌ Query database or repositories
 * ❌ Add side effects
 *
 * ------------------------------------------------------------
 * INVARIANTS (MUST NOT BREAK)
 * ------------------------------------------------------------
 * - Meal is a TEMPLATE, not an occurrence
 * - Anchor resolution must remain PURE
 * - Output depends ONLY on:
 *     Meal.type
 *     Meal.treatAsAnchor
 *
 * ------------------------------------------------------------
 * WHY THIS MATTERS
 * ------------------------------------------------------------
 * This function is used by:
 * - Supplement scheduling
 * - Activity scheduling
 * - Future meal scheduling
 *
 * If you introduce time logic here:
 * 👉 you will corrupt the entire scheduling pipeline
 *
 * ------------------------------------------------------------
 * FUTURE EXTENSIONS (SAFE)
 * ------------------------------------------------------------
 * You MAY:
 * - Expand mapping rules (if new MealTypes added)
 * - Add logging (debug only, no logic changes)
 *
 * You MUST NOT:
 * - Introduce density, heuristics, or guessing
 * - Infer anchors from names, notes, or nutrition
 *
 * ------------------------------------------------------------
 * TL;DR
 * ------------------------------------------------------------
 * PURE mapping:
 * Meal → TimeAnchor?
 *
 * NOTHING ELSE.
 */