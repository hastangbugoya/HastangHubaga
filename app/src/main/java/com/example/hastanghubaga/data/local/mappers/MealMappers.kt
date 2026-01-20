package com.example.hastanghubaga.data.local.mappers


import com.example.hastanghubaga.data.local.entity.meal.MealEntity
import com.example.hastanghubaga.data.local.entity.meal.MealNutritionEntity
import com.example.hastanghubaga.data.local.models.MealJoinedRoom
import com.example.hastanghubaga.data.time.JavaTimeAdapter
import com.example.hastanghubaga.domain.model.meal.Meal
import com.example.hastanghubaga.domain.model.meal.MealNutrition
import com.example.hastanghubaga.domain.model.meal.MealType
import com.example.hastanghubaga.ui.timeline.TimelineItem
import java.time.Instant
import java.time.ZoneId

// ------------------------------------------------------------
// MealNutritionEntity → Domain
// ------------------------------------------------------------
fun MealNutritionEntity.toDomain(): MealNutrition =
    MealNutrition(
        protein = protein,
        carbs = carbs,
        fat = fat,
        calories = calories.toDouble(),
        sodium = sodium,
        cholesterol = cholesterol,
        fiber = fiber
    )

// ------------------------------------------------------------
// MealJoinedRoom → Domain Meal
// ------------------------------------------------------------
fun MealJoinedRoom.toDomain(): Meal =
    Meal(
        id = meal.id,
        type = meal.type,
        timestamp = JavaTimeAdapter.utcMillisToDomainLocalDateTime(meal.timestamp),
        notes = meal.notes,
        nutrition = nutrition?.toDomain()
    )

// ------------------------------------------------------------
// Domain → MealEntity
// ------------------------------------------------------------
fun Meal.toEntity(): MealEntity =
    MealEntity(
        id = id,
        type = type,
        timestamp = JavaTimeAdapter.domainLocalDateTimeToUtcMillis(timestamp),
        notes = notes
    )

// ------------------------------------------------------------
// Domain → MealNutritionEntity
// ------------------------------------------------------------
fun Meal.toNutritionEntity(): MealNutritionEntity? =
    nutrition?.let {
        MealNutritionEntity(
            mealId = id,  // Repository will fill correct ID after insert
            protein = it.protein ?: 0.0,
            carbs = it.carbs ?: 0.0,
            fat = it.fat ?: 0.0,
            calories = it.calories?.toInt() ?: 0,
            sodium = it.sodium,
            cholesterol = it.cholesterol,
            fiber = it.fiber
        )
    }

fun MealType.toEntity(): com.example.hastanghubaga.data.local.entity.meal.MealType =
    when (this) {
        MealType.BREAKFAST -> com.example.hastanghubaga.data.local.entity.meal.MealType.BREAKFAST
        MealType.LUNCH -> com.example.hastanghubaga.data.local.entity.meal.MealType.LUNCH
        MealType.DINNER -> com.example.hastanghubaga.data.local.entity.meal.MealType.DINNER
        MealType.SNACK -> com.example.hastanghubaga.data.local.entity.meal.MealType.SNACK
        MealType.PRE_WORKOUT -> com.example.hastanghubaga.data.local.entity.meal.MealType.PRE_WORKOUT
        MealType.POST_WORKOUT -> com.example.hastanghubaga.data.local.entity.meal.MealType.POST_WORKOUT
        MealType.CUSTOM -> com.example.hastanghubaga.data.local.entity.meal.MealType.CUSTOM
    }


