package com.example.hastanghubaga.data.local.mappers

import com.example.hastanghubaga.data.local.entity.meal.MealEntity
import com.example.hastanghubaga.data.local.entity.meal.MealNutritionEntity
import com.example.hastanghubaga.data.local.models.MealJoinedRoom
import com.example.hastanghubaga.domain.model.Meal
import com.example.hastanghubaga.domain.model.MealNutrition
import com.example.hastanghubaga.domain.model.MealType
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

fun MealJoinedRoom.toDomain(): Meal =
    Meal(
        id = meal.id,
        type = meal.type,
        timestamp = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(meal.timestamp),
            ZoneId.systemDefault()
        ),
        nutrition = MealNutrition(
            protein = nutrition.protein,
            carbs = nutrition.carbs,
            fat = nutrition.fat,
            calories = nutrition.calories,
            sodium = nutrition.sodium,
            cholesterol = nutrition.cholesterol,
            fiber = nutrition.fiber
        ),
        notes = meal.notes
    )

fun Meal.toEntity(): Pair<MealEntity, MealNutritionEntity> {
    val mealEntity = MealEntity(
        id = id,
        type = type,
        timestamp = timestamp.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
        notes = notes
    )

    val nutritionEntity = MealNutritionEntity(
        mealId = id,
        protein = nutrition.protein,
        carbs = nutrition.carbs,
        fat = nutrition.fat,
        calories = nutrition.calories,
        sodium = nutrition.sodium,
        cholesterol = nutrition.cholesterol,
        fiber = nutrition.fiber
    )

    return mealEntity to nutritionEntity
}
