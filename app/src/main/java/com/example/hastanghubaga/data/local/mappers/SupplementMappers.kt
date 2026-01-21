package com.example.hastanghubaga.data.local.mappers

import com.example.hastanghubaga.data.local.entity.supplement.IngredientEntity
import com.example.hastanghubaga.data.local.entity.supplement.SupplementDailyLogEntity
import com.example.hastanghubaga.data.local.entity.supplement.SupplementEntity
import com.example.hastanghubaga.domain.model.supplement.Ingredient
import com.example.hastanghubaga.domain.model.supplement.Supplement
import com.example.hastanghubaga.domain.model.supplement.SupplementDailyLog
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

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
        lastTakenDate = lastTakenDate,
        ingredients = emptyList(),
        isActive = isActive,
        doseAnchorType = doseAnchorType,
        startDate = startDate,
        sendAlert = sendAlert,
        alertOffsetMinutes = alertOffsetMinutes
    )

fun com.example.hastanghubaga.domain.model.supplement.Supplement.toDomain(): com.example.hastanghubaga.data.local.entity.supplement.SupplementEntity =
    SupplementEntity(
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
        isActive = isActive,
        doseAnchorType = doseAnchorType,
        startDate = startDate,
        sendAlert = sendAlert,
        alertOffsetMinutes = alertOffsetMinutes
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

fun LocalTime.toLocalTimeLong() = this.atDate(LocalDate.now())
    .atZone(ZoneId.systemDefault())
    .toInstant()
    .toEpochMilli()

fun SupplementDailyLogEntity.toDomain() = SupplementDailyLog(
    supplementId = this.id,
    date = this.date,
    actualServingTaken = this.actualServingTaken,
    doseUnit = this.doseUnit,
    timestamp = this.timestamp
)

fun SupplementDailyLog.toEntity() = SupplementDailyLogEntity(
    supplementId = this.supplementId,
    date = this.date,
    actualServingTaken = this.actualServingTaken,
    doseUnit = this.doseUnit,
    timestamp = this.timestamp
)

