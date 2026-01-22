package com.example.hastanghubaga.domain.model.nutrition

import com.example.hastanghubaga.data.local.entity.user.UserNutritionGoalsEntity

fun UserNutritionGoalsEntity.toDomain(): UserNutritionGoals =
    UserNutritionGoals(
        id = id,
        type = type,
        name = name,
        dailyProteinTarget = dailyProteinTarget,
        dailyFatTarget = dailyFatTarget,
        dailyCarbTarget = dailyCarbTarget,
        dailyCalorieTarget = dailyCalorieTarget,
        sodiumLimitMg = sodiumLimitMg,
        cholesterolLimitMg = cholesterolLimitMg,
        fiberTargetGrams = fiberTargetGrams,
        isActive = isActive,
        startDate = startDate,
        endDate = endDate
    )
