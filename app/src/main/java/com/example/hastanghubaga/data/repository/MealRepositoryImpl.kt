package com.example.hastanghubaga.data.repository

import com.example.hastanghubaga.data.local.dao.meal.MealEntityDao
import com.example.hastanghubaga.data.local.dao.meal.MealNutritionDao
import com.example.hastanghubaga.data.local.entity.meal.MealEntity
import com.example.hastanghubaga.data.local.entity.meal.MealNutritionEntity
import com.example.hastanghubaga.data.local.entity.meal.MealType
import com.example.hastanghubaga.data.local.mappers.toDomain
import com.example.hastanghubaga.domain.model.meal.Meal
import com.example.hastanghubaga.domain.repository.meal.MealRepository
import com.example.hastanghubaga.domain.time.TimePolicy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class MealRepositoryImpl @Inject constructor(
    private val mealEntityDao: MealEntityDao,
    private val nutritionDao: MealNutritionDao
) : MealRepository {

    override fun observeAll(): Flow<List<Meal>> =
        mealEntityDao.observeAllMeals().map { list -> list.map { it.toDomain() } }

    override fun observeMeal(id: Long): Flow<Meal?> =
        mealEntityDao.observeMeal(id).map { it?.toDomain() }

    override suspend fun getMealsForDate(date: LocalDate): List<Meal> {
        val startAndEnd = TimePolicy.utcRangeForLocalDate(date)
        val startUtc = startAndEnd.first
        val endUtc = startAndEnd.second
        return mealEntityDao.getMealsForDayOnce(startUtc, endUtc).map { it.toDomain() }
    }


    override fun observeMealsForDate(date: LocalDate): Flow<List<Meal>> {
        return mealEntityDao
            .observeMealsForDate(date.toString())
            .map { joinedList -> joinedList.map { it.toDomain() }
    }
    }

    override suspend fun addMeal(
        meal: MealEntity,
        nutrition: MealNutritionEntity
    ): Long {
        val mealId = mealEntityDao.insertMeal(meal)
        nutritionDao.insertNutrition(nutrition.copy(mealId = mealId))
        return mealId
    }

    override suspend fun deleteMeal(meal: MealEntity) {
        mealEntityDao.deleteNutrition(meal.id)
        mealEntityDao.deleteMeal(meal)
    }

    override suspend fun getMealsByType(type: MealType): List<Meal> {
        val startAndEnd = TimePolicy.utcRangeForLocalDate(LocalDate.now())
        val startUtc = startAndEnd.first
        val endUtc = startAndEnd.second
        return mealEntityDao.getMealsForDayOnce(startUtc, endUtc).map { it.toDomain() }.filter { it.type == type }
    }
}
