package com.example.hastanghubaga.domain.usecase.user

import com.example.hastanghubaga.data.local.entity.user.UserNutritionGoalsEntity
import javax.inject.Inject
/**
 * Creates a new [UserNutritionGoalsEntity] by duplicating an existing goal while
 * allowing selected metadata to change (name, date range, active state).
 *
 * ---
 * ## Why this exists
 * Nutrition goals are treated as **historical configurations**, not mutable settings.
 * Once a goal has been used to evaluate days, meals, or aggregates, modifying it
 * retroactively would invalidate past data and make historical views ambiguous.
 *
 * Instead of editing goals in place, the app **duplicates** them:
 * - Past days continue to reference the old goal exactly as it was.
 * - New days use the duplicated goal with updated parameters.
 *
 * This mirrors how training blocks, diet phases, and programs work in real life
 * (e.g. "Winter Cut", "Maintenance Phase", "Lean Bulk v2").
 *
 * ---
 * ## What is intentionally duplicated
 * - Macro targets (protein, fat, carbs, calories)
 * - Optional nutrition limits (sodium, cholesterol, fiber)
 * - Goal type (CUT / MAINTENANCE / BULK, etc.)
 *
 * These values represent the *strategy* and should carry forward unchanged.
 *
 * ---
 * ## What is intentionally NOT duplicated
 * - Primary key (id)
 * - Name (caller must supply a new one)
 * - Start / end dates
 * - Active state
 *
 * These fields define *when* and *how* the goal is applied, not what the goal is.
 *
 * ---
 * ## Design notes / future safety
 * - This avoids silent data corruption from editing active goals.
 * - It enables clean goal history, comparisons, and future analytics.
 * - If you ever allow "edit goal", it should internally call this use case.
 *
 * ---
 * ## Usage example
 * Duplicate an existing goal to start a new phase:
 *
 * ```
 * duplicateUserNutritionGoals(
 *   goal = currentGoal,
 *   newName = "Spring Cut",
 *   newStartDate = todayMillis,
 *   newEndDate = null,
 *   setToActive = true
 * )
 * ```
 */
class DuplicateUserNutritionGoals @Inject constructor(){
    operator fun invoke(
        goal: UserNutritionGoalsEntity,
        newName: String,
        newStartDate: Long,
        newEndDate: Long?,
        setToActive: Boolean
    ): UserNutritionGoalsEntity {
        return UserNutritionGoalsEntity(
            name = newName,
            startDate = newStartDate,
            endDate = newEndDate,
            type = goal.type,
            dailyProteinTarget = goal.dailyProteinTarget,
            dailyFatTarget = goal.dailyFatTarget,
            dailyCarbTarget = goal.dailyCarbTarget,
            dailyCalorieTarget = goal.dailyCalorieTarget,
            sodiumLimitMg = goal.sodiumLimitMg,
            cholesterolLimitMg = goal.cholesterolLimitMg,
            fiberTargetGrams = goal.fiberTargetGrams,
            isActive = setToActive
        )
    }
}