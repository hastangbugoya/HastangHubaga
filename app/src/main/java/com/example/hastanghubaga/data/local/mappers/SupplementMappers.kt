package com.example.hastanghubaga.data.local.mappers

import com.example.hastanghubaga.data.local.entity.supplement.IngredientEntity
import com.example.hastanghubaga.data.local.entity.supplement.SupplementDailyLogEntity
import com.example.hastanghubaga.data.local.entity.supplement.SupplementEntity
import com.example.hastanghubaga.domain.model.supplement.Ingredient
import com.example.hastanghubaga.domain.model.supplement.Supplement
import com.example.hastanghubaga.domain.model.supplement.SupplementDoseLog
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

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

fun Supplement.toDomain(): SupplementEntity =
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
fun IngredientEntity.toDomain(): Ingredient =
    Ingredient(
        id = id,
        name = name,
        defaultUnit = defaultUnit,
        rdaValue = rdaValue,
        rdaUnit = rdaUnit,
        upperLimitValue = upperLimitValue,
        upperLimitUnit = upperLimitUnit,
        category = category
    )

/**
 * Convenience helper retained for existing callers that need a millis value for "today at this time".
 *
 * Prefer more explicit date + time conversion when the target date matters.
 */
fun LocalTime.toLocalTimeLong(): Long =
    LocalDateTime(
        date = LocalDate(1970, 1, 1),
        time = this
    )
        .toInstant(TimeZone.currentSystemDefault())
        .toEpochMilliseconds()

fun SupplementDailyLogEntity.toDomain(): SupplementDoseLog =
    SupplementDoseLog(
        id = id,
        supplementId = supplementId,
        date = LocalDate.parse(date),
        actualServingTaken = actualServingTaken,
        doseUnit = doseUnit,
        timestamp = Instant
            .fromEpochMilliseconds(timestamp)
            .toLocalDateTime(TimeZone.currentSystemDefault()),
        occurrenceId = occurrenceId
    )

fun SupplementDoseLog.toEntity(): SupplementDailyLogEntity =
    SupplementDailyLogEntity(
        id = id,
        supplementId = supplementId,
        date = date.toString(),
        actualServingTaken = actualServingTaken,
        doseUnit = doseUnit,
        timestamp = timestamp
            .toInstant(TimeZone.currentSystemDefault())
            .toEpochMilliseconds(),
        occurrenceId = occurrenceId
    )