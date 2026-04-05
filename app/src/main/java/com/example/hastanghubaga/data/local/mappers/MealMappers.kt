package com.example.hastanghubaga.data.local.mappers

import com.example.hastanghubaga.data.local.entity.meal.MealEntity
import com.example.hastanghubaga.data.local.entity.meal.MealNutritionEntity
import com.example.hastanghubaga.data.local.models.MealJoinedRoom
import com.example.hastanghubaga.domain.model.meal.Meal
import com.example.hastanghubaga.domain.model.meal.MealNutrition

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
        name = meal.name,
        type = meal.type,
        treatAsAnchor = meal.treatAsAnchor,
        isActive = meal.isActive,
        nutrition = nutrition?.toDomain(),
        notes = null
    )

// ------------------------------------------------------------
// Domain → MealEntity
// ------------------------------------------------------------
fun Meal.toEntity(): MealEntity =
    MealEntity(
        id = id,
        name = name,
        type = type,
        treatAsAnchor = treatAsAnchor,
        isActive = isActive
    )

// ------------------------------------------------------------
// Domain → MealNutritionEntity
// ------------------------------------------------------------
fun Meal.toNutritionEntity(): MealNutritionEntity? =
    nutrition
        ?.takeIf { it.hasAnyValue() }
        ?.let {
            MealNutritionEntity(
                mealId = id,
                protein = it.protein ?: 0.0,
                carbs = it.carbs ?: 0.0,
                fat = it.fat ?: 0.0,
                calories = it.calories?.toInt() ?: 0,
                sodium = it.sodium,
                cholesterol = it.cholesterol,
                fiber = it.fiber
            )
        }

private fun MealNutrition.hasAnyValue(): Boolean =
    protein != null ||
            carbs != null ||
            fat != null ||
            calories != null ||
            sodium != null ||
            cholesterol != null ||
            fiber != null