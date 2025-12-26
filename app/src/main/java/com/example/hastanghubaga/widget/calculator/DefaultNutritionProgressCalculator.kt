package com.example.hastanghubaga.widget.calculator

import com.example.hastanghubaga.domain.model.nutrition.NutrientGoal
import com.example.hastanghubaga.widget.model.WidgetIngredientProgress
import com.example.hastanghubaga.widget.model.WidgetIngredientProgressType
import javax.inject.Inject

class DefaultNutritionProgressCalculator @Inject constructor() :
    NutritionProgressCalculator {

    override fun calculate(
        consumed: Double,
        goal: NutrientGoal?
    ): WidgetIngredientProgress? {

        if (goal == null || !goal.isEnabled || goal.target <= 0.0) {
            return null
        }

        val percent = (consumed / goal.target) * 100.0

        return WidgetIngredientProgress(
            current = consumed,
            target = goal.target,
            percent = percent.coerceAtLeast(0.0),
            exceeded = goal.upperLimit?.let { consumed > it }
                ?: (consumed > goal.target),
            status = when {
                percent < 50.0 -> WidgetIngredientProgressType.LOW
                percent < 90.0 -> WidgetIngredientProgressType.OK
                percent <= 110.0 -> WidgetIngredientProgressType.GOOD
                else -> WidgetIngredientProgressType.EXCEEDED
            }
        )
    }
}
