package com.example.hastanghubaga.data.local.mappers

import com.example.hastanghubaga.data.local.entity.user.NutrientGoalEntity
import com.example.hastanghubaga.domain.model.nutrition.NutrientGoal


fun NutrientGoalEntity.toDomain() : NutrientGoal =
    NutrientGoal(
        ingredientId = ingredientId,
        target = target,
        upperLimit = upperLimit,
        unit = unit,
        isEnabled = isEnabled
    )
