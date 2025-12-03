package com.example.hastanghubaga.data.local.mappers

import com.example.hastanghubaga.data.local.entity.supplement.IngredientEntity
import com.example.hastanghubaga.data.local.entity.supplement.SupplementEntity
import com.example.hastanghubaga.domain.model.Ingredient
import com.example.hastanghubaga.domain.model.Supplement

// Supplement
fun SupplementEntity.toDomain(): Supplement =
    Supplement(
        id = id,
        name = name,
        brand = brand,
        notes = notes,

        recommendedServingSize = recommendedServingSize,
        recommendedDoseUnit = recommendedDoseUnit,
        servingsPerDay = servingsPerDay,
        recommendedWithFood = recommendedWithFood,
        recommendedLiquidInOz = recommendedLiquidInOz,
        recommendedTimeBetweenDailyDosesMinutes = recommendedTimeBetweenDailyDosesMinutes,
        avoidCaffeine = avoidCaffeine,

        frequencyType = frequencyType,
        frequencyInterval = frequencyInterval,
        weeklyDays = weeklyDays,
        offsetMinutes = offsetMinutes,

        ingredients = emptyList()
    )

// Ingredient
fun IngredientEntity.toDomain() = Ingredient(
    id = this.id,
    name = this.name,
    defaultUnit = this.defaultUnit,
    rdaValue = this.rdaValue,
    rdaUnit = this.rdaUnit,
    upperLimitValue = this.upperLimitValue,
    upperLimitUnit = this.upperLimitUnit,
    category = this.category
)
