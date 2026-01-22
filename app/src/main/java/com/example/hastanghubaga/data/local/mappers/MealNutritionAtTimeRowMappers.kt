package com.example.hastanghubaga.data.local.mappers

import com.example.hastanghubaga.data.local.dao.meal.MealNutritionAtTimeRow
import com.example.hastanghubaga.domain.model.meal.MealNutrition


/**
 * DB projection row -> domain nutrition (keeps timestamp for day grouping).
 *
 * Keep this in the data layer. The "timestamp + nutrition" pair is used by
 * daily/monthly aggregation pipelines.
 */
internal fun MealNutritionAtTimeRow.toTimedNutrition(): TimedNutrition =
    TimedNutrition(
        timestamp = timestamp,
        nutrition = MealNutrition(
            protein = protein.zeroToNull(),
            carbs = carbs.zeroToNull(),
            fat = fat.zeroToNull(),
            calories = calories.toDouble().zeroToNull(),
            sodium = sodium,           // already nullable
            cholesterol = cholesterol, // already nullable
            fiber = fiber              // already nullable
        )
    )

/** Small helper type for aggregation. Keep internal to data layer. */
internal data class TimedNutrition(
    val timestamp: Long,
    val nutrition: MealNutrition
)

private fun Double.zeroToNull(): Double? = if (this == 0.0) null else this

internal fun MealNutritionAtTimeRow.toDomain(): MealNutrition =
    MealNutrition(
        protein = protein.zeroToNull(),
        carbs = carbs.zeroToNull(),
        fat = fat.zeroToNull(),
        calories = calories.toDouble().zeroToNull(),
        sodium = sodium,
        cholesterol = cholesterol,
        fiber = fiber
    )

