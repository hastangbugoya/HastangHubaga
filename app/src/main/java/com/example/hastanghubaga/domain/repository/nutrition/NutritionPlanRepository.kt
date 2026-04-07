package com.example.hastanghubaga.domain.repository.nutrition

import com.example.hastanghubaga.domain.model.nutrition.NutritionGoalType
import com.example.hastanghubaga.domain.model.nutrition.NutritionPlan
import com.example.hastanghubaga.domain.model.nutrition.NutritionPlanGoal
import com.example.hastanghubaga.domain.model.nutrition.NutritionPlanWithGoals
import kotlinx.coroutines.flow.Flow

/**
 * Repository contract for HH nutrition plans.
 *
 * This repository is intentionally centered on normalized plan storage:
 * - one parent plan
 * - many child nutrient goal rows
 *
 * It supports:
 * - multiple plans
 * - multiple active plans
 * - source-tagged imported plans living in the same system as local plans
 *
 * Important:
 * Effective active-plan resolution is NOT the responsibility of basic CRUD.
 * That will be layered above this repository later.
 */
interface NutritionPlanRepository {

    // ---------------------------------------------------------
    // OBSERVE
    // ---------------------------------------------------------

    fun observeAllPlans(): Flow<List<NutritionPlan>>

    fun observeActivePlans(): Flow<List<NutritionPlan>>

    fun observePlan(planId: Long): Flow<NutritionPlan?>

    fun observePlanWithGoals(planId: Long): Flow<NutritionPlanWithGoals?>

    // ---------------------------------------------------------
    // READ ONCE
    // ---------------------------------------------------------

    suspend fun getAllPlans(): List<NutritionPlan>

    suspend fun getActivePlans(): List<NutritionPlan>

    suspend fun getPlan(planId: Long): NutritionPlan?

    suspend fun getPlanWithGoals(planId: Long): NutritionPlanWithGoals?

    suspend fun getPlansByType(type: NutritionGoalType): List<NutritionPlan>

    suspend fun getPlansBySourceType(sourceType: String): List<NutritionPlan>

    suspend fun getPlanBySource(
        sourceType: String,
        sourcePlanId: String
    ): NutritionPlan?

    suspend fun getPlansEffectiveOn(dateMillis: Long): List<NutritionPlan>

    suspend fun getActivePlansEffectiveOn(dateMillis: Long): List<NutritionPlan>

    suspend fun getGoalsForPlan(planId: Long): List<NutritionPlanGoal>

    // ---------------------------------------------------------
    // WRITE
    // ---------------------------------------------------------

    /**
     * Creates a new plan and all of its child nutrient goal rows.
     *
     * Returns the new plan ID.
     */
    suspend fun createPlan(
        plan: NutritionPlan,
        goals: List<NutritionPlanGoal>
    ): Long

    /**
     * Updates the parent plan metadata only.
     */
    suspend fun updatePlan(plan: NutritionPlan)

    /**
     * Replaces the full child-goal set for a plan.
     *
     * Intended for edit/save flows where the latest submitted set becomes
     * the source of truth for that plan.
     */
    suspend fun replacePlanGoals(
        planId: Long,
        goals: List<NutritionPlanGoal>
    )

    /**
     * Convenience write for updating both the parent plan and its goals together.
     */
    suspend fun updatePlanWithGoals(
        plan: NutritionPlan,
        goals: List<NutritionPlanGoal>
    )

    suspend fun deletePlan(planId: Long)

    suspend fun setPlanActiveState(
        planId: Long,
        isActive: Boolean
    )

    suspend fun upsertPlanGoal(
        planId: Long,
        goal: NutritionPlanGoal
    )

    suspend fun deletePlanGoal(
        planId: Long,
        nutrientKey: String
    )
}