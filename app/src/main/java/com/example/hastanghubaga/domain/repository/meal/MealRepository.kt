package com.example.hastanghubaga.domain.repository.meal

import com.example.hastanghubaga.domain.model.Meal
import com.example.hastanghubaga.domain.model.MealType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface MealRepository {

    fun observeAll(): Flow<List<Meal>>

    fun observeMeal(id: Long): Flow<Meal?>

    suspend fun getMealsForDate(date: LocalDate): List<Meal>

    suspend fun addMeal(meal: Meal): Long

    suspend fun deleteMeal(meal: Meal)

    suspend fun getMealsByType(type: MealType): List<Meal>
}
