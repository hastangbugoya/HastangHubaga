package com.example.hastanghubaga.domain.repository.nutrition

import com.example.hastanghubaga.domain.model.nutrition.NutrientGoal
import com.example.hastanghubaga.domain.model.nutrition.NutritionGoal


interface NutritionPlanRepository {
    suspend fun getGoalsForIngredientIds(
        ingredientIds: List<Long>
    ): Map<Long, NutrientGoal>
}