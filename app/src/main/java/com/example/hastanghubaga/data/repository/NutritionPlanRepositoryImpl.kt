package com.example.hastanghubaga.data.repository

import com.example.hastanghubaga.domain.model.nutrition.NutritionGoalType
import com.example.hastanghubaga.domain.model.nutrition.NutritionPlan
import com.example.hastanghubaga.domain.model.nutrition.NutritionPlanGoal
import com.example.hastanghubaga.domain.model.nutrition.NutritionPlanWithGoals
import com.example.hastanghubaga.domain.repository.nutrition.NutritionPlanRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Legacy compatibility wrapper.
 *
 * The old NutritionPlanRepositoryImpl was built for the obsolete
 * ingredient-based nutrition goal API. HH now uses the normalized
 * multi-plan implementation in [NutritionGoalsRepositoryImpl].
 *
 * This wrapper keeps the old class name compiling while delegating all
 * behavior to the new implementation.
 *
 * Future cleanup:
 * - Remove this file after all references have been migrated to
 *   NutritionGoalsRepositoryImpl directly.
 */
@Singleton
class NutritionPlanRepositoryImpl @Inject constructor(
    private val delegate: NutritionGoalsRepositoryImpl
) : NutritionPlanRepository {

    override fun observeAllPlans(): Flow<List<NutritionPlan>> =
        delegate.observeAllPlans()

    override fun observeActivePlans(): Flow<List<NutritionPlan>> =
        delegate.observeActivePlans()

    override fun observePlan(planId: Long): Flow<NutritionPlan?> =
        delegate.observePlan(planId)

    override fun observePlanWithGoals(planId: Long): Flow<NutritionPlanWithGoals?> =
        delegate.observePlanWithGoals(planId)

    override suspend fun getAllPlans(): List<NutritionPlan> =
        delegate.getAllPlans()

    override suspend fun getActivePlans(): List<NutritionPlan> =
        delegate.getActivePlans()

    override suspend fun getPlan(planId: Long): NutritionPlan? =
        delegate.getPlan(planId)

    override suspend fun getPlanWithGoals(planId: Long): NutritionPlanWithGoals? =
        delegate.getPlanWithGoals(planId)

    override suspend fun getPlansByType(type: NutritionGoalType): List<NutritionPlan> =
        delegate.getPlansByType(type)

    override suspend fun getPlansBySourceType(sourceType: String): List<NutritionPlan> =
        delegate.getPlansBySourceType(sourceType)

    override suspend fun getPlanBySource(
        sourceType: String,
        sourcePlanId: String
    ): NutritionPlan? =
        delegate.getPlanBySource(sourceType, sourcePlanId)

    override suspend fun getPlansEffectiveOn(dateMillis: Long): List<NutritionPlan> =
        delegate.getPlansEffectiveOn(dateMillis)

    override suspend fun getActivePlansEffectiveOn(dateMillis: Long): List<NutritionPlan> =
        delegate.getActivePlansEffectiveOn(dateMillis)

    override suspend fun getGoalsForPlan(planId: Long): List<NutritionPlanGoal> =
        delegate.getGoalsForPlan(planId)

    override suspend fun createPlan(
        plan: NutritionPlan,
        goals: List<NutritionPlanGoal>
    ): Long =
        delegate.createPlan(plan, goals)

    override suspend fun updatePlan(plan: NutritionPlan) {
        delegate.updatePlan(plan)
    }

    override suspend fun replacePlanGoals(
        planId: Long,
        goals: List<NutritionPlanGoal>
    ) {
        delegate.replacePlanGoals(planId, goals)
    }

    override suspend fun updatePlanWithGoals(
        plan: NutritionPlan,
        goals: List<NutritionPlanGoal>
    ) {
        delegate.updatePlanWithGoals(plan, goals)
    }

    override suspend fun deletePlan(planId: Long) {
        delegate.deletePlan(planId)
    }

    override suspend fun setPlanActiveState(
        planId: Long,
        isActive: Boolean
    ) {
        delegate.setPlanActiveState(planId, isActive)
    }

    override suspend fun upsertPlanGoal(
        planId: Long,
        goal: NutritionPlanGoal
    ) {
        delegate.upsertPlanGoal(planId, goal)
    }

    override suspend fun deletePlanGoal(
        planId: Long,
        nutrientKey: String
    ) {
        delegate.deletePlanGoal(planId, nutrientKey)
    }
}