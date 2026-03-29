package com.example.hastanghubaga.domain.usecase.meal

import com.example.hastanghubaga.data.local.entity.meal.MealType
import com.example.hastanghubaga.domain.model.meal.Meal
import com.example.hastanghubaga.domain.schedule.model.TimeAnchor

/**
 * Resolves a [Meal] into a [TimeAnchor] if applicable.
 *
 * This is a pure function that determines whether a meal can act as an anchor
 * for future scheduling logic.
 *
 * ## Rules (minimal, first-pass)
 * 1. If [Meal.treatAsAnchor] is set → it overrides everything
 * 2. Else if [Meal.type] is BREAKFAST / LUNCH / DINNER → map to anchor
 * 3. Else → return null (meal is not an anchor)
 *
 * ## Important
 * - This does NOT affect timeline rendering
 * - This does NOT introduce scheduling
 * - This ONLY prepares meals to become anchor providers later
 */
class ResolveMealAnchorUseCase {

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