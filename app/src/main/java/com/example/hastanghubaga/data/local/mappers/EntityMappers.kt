package com.example.hastanghubaga.data.local.mappers

import com.example.hastanghubaga.data.local.entity.supplement.IngredientEntity
import com.example.hastanghubaga.data.local.entity.supplement.SupplementEntity
import com.example.hastanghubaga.data.local.entity.supplement.SupplementWithSettings
import com.example.hastanghubaga.data.local.entity.user.SupplementUserSettingsEntity
import com.example.hastanghubaga.data.local.models.SupplementJoinedRoom
import com.example.hastanghubaga.domain.model.supplement.Ingredient
import com.example.hastanghubaga.domain.model.supplement.Supplement
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

// Supplement
fun SupplementEntity.toSupplementSettings(): Supplement =
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
        lastTakenDate = lastTakenDate,
        ingredients = emptyList(),
        isActive = isActive,
        doseAnchorType = doseAnchorType,
        startDate = startDate
    )

// Ingredient
fun IngredientEntity.toSupplementSettings() = Ingredient(
    id = this.id,
    name = this.name,
    defaultUnit = this.defaultUnit,
    rdaValue = this.rdaValue,
    rdaUnit = this.rdaUnit,
    upperLimitValue = this.upperLimitValue,
    upperLimitUnit = this.upperLimitUnit,
    category = this.category
)

fun LocalTime.toLocalTimeLong() = this.atDate(LocalDate.now())
    .atZone(ZoneId.systemDefault())
    .toInstant()
    .toEpochMilli()

