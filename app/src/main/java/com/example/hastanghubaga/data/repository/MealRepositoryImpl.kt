package com.example.hastanghubaga.data.repository

import com.example.hastanghubaga.data.local.dao.meal.MealDao
import com.example.hastanghubaga.data.local.mappers.toDomain
import com.example.hastanghubaga.data.local.mappers.toEntity
import com.example.hastanghubaga.domain.model.Meal
import com.example.hastanghubaga.domain.model.MealType
import com.example.hastanghubaga.domain.repository.meal.MealRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class MealRepositoryImpl @Inject constructor(
    private val dao: MealDao
) : MealRepository {

    override fun observeAll(): Flow<List<Meal>> =
        dao.observeAllMeals().map { list -> list.map { it.toDomain() } }

    override fun observeMeal(id: Long): Flow<Meal?> =
        dao.observeMeal(id).map { it?.toDomain() }

    override suspend fun getMealsForDate(date: LocalDate): List<Meal> =
        dao.getMealsForDate(date.toString()).map { it.toDomain() }

    override suspend fun addMeal(meal: Meal): Long {
        val (mealEntity, nutrition) = meal.toEntity()

        val mealId = dao.insertMeal(mealEntity)
        dao.insertNutrition(nutrition.copy(mealId = mealId))

        return mealId
    }

    override suspend fun deleteMeal(meal: Meal) {
        dao.deleteNutrition(meal.id)
        dao.deleteMeal(meal.toEntity().first)
    }

    override suspend fun getMealsByType(type: MealType): List<Meal> =
        dao.getMealsForDate(LocalDate.now().toString())
            .map { it.toDomain() }
            .filter { it.type == type }
}
