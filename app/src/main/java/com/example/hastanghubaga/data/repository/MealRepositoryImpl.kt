package com.example.hastanghubaga.data.repository

import com.example.hastanghubaga.data.local.dao.meal.MealEntityDao
import com.example.hastanghubaga.data.local.dao.meal.MealNutritionDao
import com.example.hastanghubaga.data.local.dao.meal.MealScheduleDao
import com.example.hastanghubaga.data.local.db.AppDatabase
import com.example.hastanghubaga.data.local.entity.meal.MealEntity
import com.example.hastanghubaga.data.local.entity.meal.MealNutritionEntity
import com.example.hastanghubaga.data.local.mappers.toDomain
import com.example.hastanghubaga.domain.model.meal.Meal
import com.example.hastanghubaga.domain.model.meal.MealNutrition
import com.example.hastanghubaga.domain.model.meal.NutritionInput
import com.example.hastanghubaga.domain.repository.meal.MealRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import androidx.room.withTransaction
import kotlinx.coroutines.flow.first

/**
 * Repository implementation for HH native meal templates.
 *
 * Meals are now moving toward the same architecture as activities:
 *
 * - MealEntity = reusable template
 * - MealScheduleEntity graph = recurrence + timing rules
 * - future occurrence/log layers = date-specific planned and actual records
 *
 * Because of that, this repository no longer treats the meals table as a
 * timestamped event table.
 */
class MealRepositoryImpl @Inject constructor(
    private val db: AppDatabase,
    private val mealEntityDao: MealEntityDao,
    private val nutritionDao: MealNutritionDao,
    private val mealScheduleDao: MealScheduleDao
) : MealRepository {

    override fun observeAll(): Flow<List<Meal>> =
        mealEntityDao.observeAllMeals().map { list -> list.map { it.toDomain() } }

    override fun observeActive(): Flow<List<Meal>> =
        mealEntityDao.observeActiveMeals().map { list -> list.map { it.toDomain() } }

    override fun observeMeal(id: Long): Flow<Meal?> =
        mealEntityDao.observeMeal(id).map { it?.toDomain() }

    override suspend fun getMealById(id: Long): Meal? =
        mealEntityDao.observeMeal(id)
            .map { it?.toDomain() }
            .first()

    override suspend fun getAllOnce(): List<Meal> =
        mealEntityDao.getAllMealsOnce().map { it.toDomain() }

    override suspend fun getActiveOnce(): List<Meal> =
        mealEntityDao.getActiveMealsOnce().map { it.toDomain() }

    override suspend fun upsertMeal(
        meal: MealEntity,
        nutrition: MealNutritionEntity
    ): Long {
        return db.withTransaction {
            val mealId = mealEntityDao.upsertMeal(meal)
            nutritionDao.insertNutrition(nutrition.copy(mealId = mealId))
            mealId
        }
    }

    override suspend fun deleteMeal(meal: MealEntity) {
        db.withTransaction {
            mealScheduleDao.deleteFullScheduleForMeal(meal.id)
            mealEntityDao.deleteNutrition(meal.id)
            mealEntityDao.deleteMeal(meal)
        }
    }

    override suspend fun deleteMealById(mealId: Long) {
        db.withTransaction {
            mealScheduleDao.deleteFullScheduleForMeal(mealId)
            mealEntityDao.deleteNutrition(mealId)
            mealEntityDao.deleteMealById(mealId)
        }
    }

    override suspend fun clearSchedule(mealId: Long) {
        mealScheduleDao.deleteFullScheduleForMeal(mealId)
    }

    override suspend fun hasSchedule(mealId: Long): Boolean =
        mealScheduleDao.getScheduleForMeal(mealId) != null

    /**
     * Transitional guardrail:
     *
     * The previous implementation wrote a timestamped meal directly into the
     * meals table. That is no longer valid because MealEntity is now a reusable
     * template, not an actual logged event.
     *
     * Native HH meal logging must be moved to a dedicated actual-meal log path
     * (or future occurrence-aware meal log table) before this can be re-enabled.
     */
    override suspend fun logMeal(
        mealId: Long?,
        timestampMillis: Long,
        notes: String?,
        nutrition: NutritionInput?
    ) {
        throw IllegalStateException(
            "Native HH meal logging is not yet wired to the new template/schedule architecture. " +
                    "MealEntity is now a template table, so actual meal logs need a dedicated log table/path."
        )
    }

    /**
     * Transitional placeholder:
     *
     * Date-based nutrition observation previously depended on meals being stored
     * as timestamped events. That is no longer true once MealEntity becomes a
     * template table. This should be reimplemented against the actual meal-log
     * storage path once that layer is added.
     */
    override fun observeMealNutritionForDate(dateMillis: Long): Flow<List<MealNutrition>> =
        flowOf(emptyList())
}