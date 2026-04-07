package com.example.hastanghubaga.data.local.mappers

import com.example.hastanghubaga.data.local.entity.user.NutrientGoalEntity
import com.example.hastanghubaga.data.local.entity.user.UserNutritionPlanEntity
import com.example.hastanghubaga.domain.model.nutrition.NutritionPlan
import com.example.hastanghubaga.domain.model.nutrition.NutritionPlanGoal
import com.example.hastanghubaga.domain.model.nutrition.NutritionPlanWithGoals

/**
 * Mapper helpers for the normalized HH nutrition-plan architecture.
 *
 * This file replaces the old wide-row nutrition goal mappers.
 *
 * Important:
 * - AK import payload normalization should happen before these mappers.
 * - These mappers only translate between HH canonical persistence models
 *   and HH canonical domain models.
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

fun NutritionPlanGoal.toEntity(
    planId: Long,
    id: Long = this.id
): NutrientGoalEntity =
    NutrientGoalEntity(
        id = id,
        planId = planId,
        nutrientKey = nutrientKey,
        minValue = minValue,
        targetValue = targetValue,
        maxValue = maxValue
    )

fun UserNutritionPlanEntity.toWithGoals(
    goalEntities: List<NutrientGoalEntity>
): NutritionPlanWithGoals =
    NutritionPlanWithGoals(
        plan = this.toDomain(),
        goals = goalEntities.map { it.toDomain() }
    )