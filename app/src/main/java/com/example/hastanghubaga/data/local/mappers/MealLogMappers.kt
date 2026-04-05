package com.example.hastanghubaga.data.local.mappers

import com.example.hastanghubaga.data.local.entity.meal.MealLogEntity
import com.example.hastanghubaga.data.time.JavaTimeAdapter
import com.example.hastanghubaga.domain.model.meal.MealLog

/**
 * Mappers for actual logged HH meals.
 *
 * Canonical meal model:
 * - MealEntity = template
 * - MealOccurrenceEntity = planned occurrence
 * - MealLogEntity = actual consumed meal
 */
fun MealLogEntity.toDomain(): MealLog =
    MealLog(
        id = id,
        mealId = mealId,
        occurrenceId = occurrenceId,
        mealType = mealType,
        start = JavaTimeAdapter.utcMillisToDomainLocalDateTime(startTimestamp),
        end = endTimestamp?.let(JavaTimeAdapter::utcMillisToDomainLocalDateTime),
        notes = notes,
        calories = calories,
        proteinGrams = proteinGrams,
        carbsGrams = carbsGrams,
        fatGrams = fatGrams,
        sodiumMg = sodiumMg,
        cholesterolMg = cholesterolMg,
        fiberGrams = fiberGrams
    )

fun MealLog.toEntity(): MealLogEntity =
    MealLogEntity(
        id = id,
        mealId = mealId,
        occurrenceId = occurrenceId,
        mealType = mealType,
        startTimestamp = JavaTimeAdapter.domainLocalDateTimeToUtcMillis(start),
        endTimestamp = end?.let(JavaTimeAdapter::domainLocalDateTimeToUtcMillis),
        notes = notes,
        calories = calories,
        proteinGrams = proteinGrams,
        carbsGrams = carbsGrams,
        fatGrams = fatGrams,
        sodiumMg = sodiumMg,
        cholesterolMg = cholesterolMg,
        fiberGrams = fiberGrams
    )

