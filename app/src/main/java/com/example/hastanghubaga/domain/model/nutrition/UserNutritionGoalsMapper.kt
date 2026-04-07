package com.example.hastanghubaga.domain.model.nutrition

import com.example.hastanghubaga.data.local.entity.user.NutrientGoalEntity
import com.example.hastanghubaga.data.local.entity.user.UserNutritionPlanEntity

/**
 * Mapper helpers for the normalized HH nutrition goal architecture.
 *
 * This file intentionally replaces the old wide-row nutrition-goal mapper.
 *
 * Current role:
 * - parent plan entity <-> domain plan
 * - child goal entity <-> domain goal
 * - combined parent + children -> domain aggregate
 *
 * Future AI/dev note:
 * - Keep AK import payload mapping OUT of these mappers.
 * - AK field/key normalization should happen before persistence, then these
 *   mappers should operate only on HH canonical models.
 */
fun UserNutritionPlanEntity.toDomain(): NutritionPlan =
    NutritionPlan(
        id = id,
        type = type,
        name = name,
        startDate = startDate,
        endDate = endDate,
        isActive = isActive,
        sourceType = sourceType,
        sourcePlanId = sourcePlanId,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

fun NutritionPlan.toEntity(): UserNutritionPlanEntity =
    UserNutritionPlanEntity(
        id = id,
        type = type,
        name = name,
        startDate = startDate,
        endDate = endDate,
        isActive = isActive,
        sourceType = sourceType,
        sourcePlanId = sourcePlanId,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

fun NutrientGoalEntity.toDomain(): NutritionPlanGoal =
    NutritionPlanGoal(
        id = id,
        nutrientKey = nutrientKey,
        minValue = minValue,
        targetValue = targetValue,
        maxValue = maxValue
    )

fun NutritionPlanGoal.toEntity(planId: Long): NutrientGoalEntity =
    NutrientGoalEntity(
        id = id,
        planId = planId,
        nutrientKey = nutrientKey,
        minValue = minValue,
        targetValue = targetValue,
        maxValue = maxValue
    )

fun NutritionPlan.toWithGoals(
    goals: List<NutritionPlanGoal>
): NutritionPlanWithGoals =
    NutritionPlanWithGoals(
        plan = this,
        goals = goals
    )

fun UserNutritionPlanEntity.toWithGoals(
    goalEntities: List<NutrientGoalEntity>
): NutritionPlanWithGoals =
    NutritionPlanWithGoals(
        plan = toDomain(),
        goals = goalEntities.map { it.toDomain() }
    )