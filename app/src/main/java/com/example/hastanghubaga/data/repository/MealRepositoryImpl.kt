package com.example.hastanghubaga.data.repository

import android.util.Log
import com.example.hastanghubaga.data.local.dao.meal.MealEntityDao
import com.example.hastanghubaga.data.local.dao.meal.MealNutritionDao
import com.example.hastanghubaga.data.local.db.AppDatabase
import com.example.hastanghubaga.data.local.entity.meal.MealEntity
import com.example.hastanghubaga.data.local.entity.meal.MealNutritionEntity
import com.example.hastanghubaga.data.local.entity.meal.MealType
import com.example.hastanghubaga.data.local.mappers.toDomain
import com.example.hastanghubaga.domain.model.meal.Meal
import com.example.hastanghubaga.domain.repository.meal.MealRepository
import com.example.hastanghubaga.data.time.DateRangeConverter
import com.example.hastanghubaga.domain.model.meal.NutritionInput
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import javax.inject.Inject
import androidx.room.withTransaction
import com.example.hastanghubaga.domain.model.meal.MealNutrition
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

class MealRepositoryImpl @Inject constructor(
    private val db: AppDatabase,
    private val mealEntityDao: MealEntityDao,
    private val nutritionDao: MealNutritionDao
) : MealRepository {

    override fun observeAll(): Flow<List<Meal>> =
        mealEntityDao.observeAllMeals().map { list -> list.map { it.toDomain() } }

    override fun observeMeal(id: Long): Flow<Meal?> =
        mealEntityDao.observeMeal(id).map { it?.toDomain() }

    override suspend fun getMealsForDate(date: LocalDate): List<Meal> {
        val (startUtc, endUtc) =
            DateRangeConverter.utcRangeForLocalDate(date)

        return mealEntityDao
            .getMealsForDayOnce(startUtc, endUtc)
            .map { it.toDomain() }
    }


    override fun observeMealsForDate(date: LocalDate): Flow<List<Meal>> {
        val (startUtc, endUtc) =
            DateRangeConverter.utcRangeForLocalDate(date)

        Log.d("MealDebug", "Query date=$date")
        Log.d("MealDebug", "UTC range: $startUtc → $endUtc")

        return mealEntityDao
            .observeMealsForDay(startUtc, endUtc)
            .map { joinedList ->

                Log.d("MealDebug", "DB returned ${joinedList.size} meals")

                joinedList.forEach {
                    Log.d(
                        "MealDebug",
                        "Meal id=${it.meal.id} type=${it.meal.type} ts=${it.meal.timestamp}"
                    )
                }

                joinedList.map { it.toDomain() }
            }
    }

    override suspend fun addMeal(
        meal: MealEntity,
        nutrition: MealNutritionEntity,
        type: MealType
    ): Long {
        val mealId = mealEntityDao.insertMeal(meal)
        nutritionDao.insertNutrition(nutrition.copy(mealId = mealId))
        return mealId
    }

    override suspend fun deleteMeal(meal: MealEntity) {
        mealEntityDao.deleteNutrition(meal.id)
        mealEntityDao.deleteMeal(meal)
    }

    override suspend fun getMealsByType(date: LocalDate, type: MealType): List<Meal> {
        val (startUtc, endUtc) =
            DateRangeConverter.utcRangeForLocalDate(date)

        return mealEntityDao
            .getMealsForDayOnce(startUtc, endUtc)
            .map { it.toDomain() }
            .filter { it.type == type }
    }

    override suspend fun logMeal(
        type: MealType,
        timestampMillis: Long,
        notes: String?,
        nutrition: NutritionInput?
    ) {
        return db.withTransaction {
            val mealId = mealEntityDao.insertMeal(
                MealEntity(
                    id = 0L,
                    type = type,
                    timestamp = timestampMillis,
                    notes = notes
                )
            )

            if (nutrition != null) {
                nutritionDao.insertNutrition(
                    MealNutritionEntity(
                        mealId = mealId,
                        calories = nutrition.calories ?: 0,
                        protein = nutrition.proteinGrams ?: 0.0,
                        carbs = nutrition.carbsGrams ?: 0.0,
                        fat = nutrition.fatGrams ?: 0.0,
                    )
                )
            }
        }
    }

    override fun observeMealNutritionForDate(dateMillis: Long): Flow<List<MealNutrition>> {
        val tz = kotlinx.datetime.TimeZone.currentSystemDefault()

        val localDate = kotlinx.datetime.Instant
            .fromEpochMilliseconds(dateMillis)
            .toLocalDateTime(tz)
            .date

        val startMillis = localDate
            .atStartOfDayIn(tz)
            .toEpochMilliseconds()

        val endMillis = localDate
            .plus(kotlinx.datetime.DatePeriod(days = 1))
            .atStartOfDayIn(tz)
            .toEpochMilliseconds()

        return nutritionDao
            .observeNutritionForMealsInRange(startMillis, endMillis)
            .map { list -> list.map { it.toDomain() } }
    }


}
