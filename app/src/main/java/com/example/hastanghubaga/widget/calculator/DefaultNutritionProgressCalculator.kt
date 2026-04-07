package com.example.hastanghubaga.widget.calculator

import com.example.hastanghubaga.domain.model.nutrition.NutritionPlanGoal
import com.example.hastanghubaga.widget.model.WidgetIngredientProgress
import com.example.hastanghubaga.widget.model.WidgetIngredientProgressType
import javax.inject.Inject

class DefaultNutritionProgressCalculator @Inject constructor() :
    NutritionProgressCalculator {

    override fun calculate(
        consumed: Double,
        goal: NutritionPlanGoal?
    ): WidgetIngredientProgress? {
        if (goal == null) {
            return null
        }

        val referenceValue = goal.targetValue
            ?: goal.maxValue
            ?: goal.minValue

        if (referenceValue == null || referenceValue <= 0.0) {
            return null
        }

        val percent = (consumed / referenceValue) * 100.0

        val exceeded = when {
            goal.maxValue != null -> consumed > goal.maxValue
            goal.targetValue != null -> consumed > goal.targetValue
            else -> false
        }

        return WidgetIngredientProgress(
            current = consumed,
            target = referenceValue,
            percent = percent.coerceAtLeast(0.0),
            exceeded = exceeded,
            status = when {
                exceeded -> WidgetIngredientProgressType.EXCEEDED
                percent < 50.0 -> WidgetIngredientProgressType.LOW
                percent < 90.0 -> WidgetIngredientProgressType.OK
                percent <= 110.0 -> WidgetIngredientProgressType.GOOD
                else -> WidgetIngredientProgressType.EXCEEDED
            }
        )
    }
}