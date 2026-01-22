package com.example.hastanghubaga.domain.repository.meal

import com.example.hastanghubaga.data.local.entity.meal.MealEntity
import com.example.hastanghubaga.data.local.entity.meal.MealNutritionEntity
import com.example.hastanghubaga.data.local.entity.meal.MealType
import com.example.hastanghubaga.domain.model.meal.Meal
import com.example.hastanghubaga.domain.model.meal.MealNutrition
import com.example.hastanghubaga.domain.model.meal.NutritionInput
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate


interface MealRepository {

    fun observeAll(): Flow<List<Meal>>

    fun observeMeal(id: Long): Flow<Meal?>

    suspend fun getMealsForDate(date: LocalDate): List<Meal>

    fun observeMealsForDate(date: LocalDate): Flow<List<Meal>>

    suspend fun addMeal(
        meal: MealEntity,
        nutrition: MealNutritionEntity,
        type: com.example.hastanghubaga.domain.model.meal.MealType
    ): Long

    suspend fun deleteMeal(meal: MealEntity)

    suspend fun getMealsByType(date: LocalDate, type: MealType): List<Meal>

    suspend fun logMeal(
        type: MealType,
        timestampMillis: Long,
        notes: String?,
        nutrition: NutritionInput?
    )

    fun observeMealNutritionForDate(dateMillis: Long): Flow<List<MealNutrition>>
}
