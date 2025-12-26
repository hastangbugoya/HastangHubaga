package com.example.hastanghubaga.data.repository

import com.example.hastanghubaga.data.local.dao.nutrition.NutrientGoalDao
import com.example.hastanghubaga.data.local.dao.nutrition.NutritionPlanEntityDao
import com.example.hastanghubaga.domain.model.nutrition.NutrientGoal
import com.example.hastanghubaga.domain.repository.nutrition.NutritionPlanRepository
import javax.inject.Inject

class NutritionPlanRepositoryImpl @Inject constructor(
    private val planDao: NutritionPlanEntityDao,
    private val goalDao: NutrientGoalDao
) : NutritionPlanRepository {

    override suspend fun getGoalsForIngredientIds(
        ingredientIds: List<Long>
    ): Map<Long, NutrientGoal> {

        if (ingredientIds.isEmpty()) return emptyMap()

        val activePlan = planDao.getActivePlan()
            ?: return emptyMap()

        val goalEntities = goalDao.getGoalsForIngredients(
            planId = activePlan.id,
            ingredientIds = ingredientIds
        )

        return goalEntities.associateBy(
            keySelector = { it.ingredientId },
            valueTransform = {
                NutrientGoal(
                    ingredientId = it.ingredientId,
                    target = it.target,
                    upperLimit = it.upperLimit,
                    unit = it.unit,
                    isEnabled = it.isEnabled
                )
            }
        )
    }
}
