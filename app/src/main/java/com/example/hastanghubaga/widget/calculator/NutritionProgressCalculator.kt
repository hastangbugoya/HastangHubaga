package com.example.hastanghubaga.widget.calculator

import com.example.hastanghubaga.domain.model.nutrition.NutritionPlanGoal
import com.example.hastanghubaga.widget.model.WidgetIngredientProgress

interface NutritionProgressCalculator {
    fun calculate(
        consumed: Double,
        goal: NutritionPlanGoal?
    ): WidgetIngredientProgress?
}