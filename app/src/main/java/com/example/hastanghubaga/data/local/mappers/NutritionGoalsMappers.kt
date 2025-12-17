package com.example.hastanghubaga.data.local.mappers


import com.example.hastanghubaga.data.local.entity.user.UserNutritionGoalsEntity
import com.example.hastanghubaga.domain.model.nutrition.NutritionGoal

fun UserNutritionGoalsEntity.toDomain(): NutritionGoal = NutritionGoal(
    id = id,
    type = type,
    name = name,
    protein = dailyProteinTarget,
    fat = dailyFatTarget,
    carbs = dailyCarbTarget,
    calories = dailyCalorieTarget,
    sodium = sodiumLimitMg,
    cholesterol = cholesterolLimitMg,
    fiber = fiberTargetGrams,
    isActive = isActive,
    start = startDate,
    end = endDate
)

fun NutritionGoal.toEntity(): UserNutritionGoalsEntity =
    UserNutritionGoalsEntity(
        id = id,
        type = type,
        name = name,
        dailyProteinTarget = protein,
        dailyCarbTarget = carbs,
        dailyFatTarget = fat,
        dailyCalorieTarget = calories,
        sodiumLimitMg = sodium,
        cholesterolLimitMg = cholesterol,
        fiberTargetGrams = fiber,
        isActive = isActive,
        startDate = start,
        endDate = end
    )