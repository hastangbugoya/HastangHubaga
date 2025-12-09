package com.example.hastanghubaga.domain.repository.meal

import com.example.hastanghubaga.domain.model.meal.Meal
import com.example.hastanghubaga.data.local.entity.meal.MealEntity
import com.example.hastanghubaga.data.local.entity.meal.MealNutritionEntity
import com.example.hastanghubaga.data.local.entity.meal.MealType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface MealRepository {

    fun observeAll(): Flow<List<Meal>>

    fun observeMeal(id: Long): Flow<Meal?>

    suspend fun getMealsForDate(date: LocalDate): List<Meal>

    suspend fun addMeal(
        meal: MealEntity,
        nutrition: MealNutritionEntity
    ): Long

    suspend fun deleteMeal(meal: MealEntity)

    suspend fun getMealsByType(type: MealType): List<Meal>
}
