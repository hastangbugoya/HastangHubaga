package com.example.hastanghubaga.data.local.mappers


import com.example.hastanghubaga.data.local.entity.meal.MealEntity
import com.example.hastanghubaga.data.local.entity.meal.MealNutritionEntity
import com.example.hastanghubaga.data.local.models.MealJoinedRoom
import com.example.hastanghubaga.domain.model.meal.Meal
import com.example.hastanghubaga.domain.model.meal.MealNutrition
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
        calories = calories,
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
        timestamp = Instant.ofEpochMilli(meal.timestamp).atZone(ZoneId.systemDefault()).toLocalDateTime(),
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
        timestamp = timestamp.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
        notes = notes
    )

// ------------------------------------------------------------
// Domain → MealNutritionEntity
// ------------------------------------------------------------
fun Meal.toNutritionEntity(): MealNutritionEntity? =
    nutrition?.let {
        MealNutritionEntity(
            mealId = id,  // Repository will fill correct ID after insert
            protein = it.protein,
            carbs = it.carbs,
            fat = it.fat,
            calories = it.calories,
            sodium = it.sodium,
            cholesterol = it.cholesterol,
            fiber = it.fiber
        )
    }