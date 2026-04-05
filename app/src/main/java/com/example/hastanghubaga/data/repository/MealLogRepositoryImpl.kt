package com.example.hastanghubaga.data.repository

import android.util.Log
import com.example.hastanghubaga.data.local.dao.meal.MealLogDao
import com.example.hastanghubaga.data.local.entity.meal.MealLogEntity
import com.example.hastanghubaga.data.local.entity.meal.MealType
import com.example.hastanghubaga.data.local.mappers.toDomain
import com.example.hastanghubaga.domain.model.meal.MealLog
import com.example.hastanghubaga.domain.repository.meal.MealLogRepository
import com.example.hastanghubaga.domain.time.DomainTimePolicy
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate

/**
 * Repository implementation for actual logged HH meals.
 *
 * Canonical meal model:
 * - MealEntity = template
 * - MealOccurrenceEntity = planned occurrence
 * - MealLogEntity = actual consumed meal
 *
 * Planned logging contract:
 * - a non-null occurrenceId identifies one specific planned occurrence
 * - saving a log for the same non-null occurrenceId must update/replace the
 *   existing log row rather than create a duplicate
 * - a null occurrenceId represents an ad-hoc / force-logged meal and is
 *   inserted as an independent row
 */
class MealLogRepositoryImpl @Inject constructor(
    private val dao: MealLogDao
) : MealLogRepository {

    override fun observeMealLogsForDate(date: LocalDate): Flow<List<MealLog>> {
        val (start, end) = DomainTimePolicy.utcMillisRangeForLocalDate(date)

        return dao.observeMealLogsForDay(
            startUtcMillis = start,
            endUtcMillis = end
        ).map { logs ->
            logs.map { it.toDomain() }
        }
    }

    override suspend fun insertMealLog(
        mealId: Long?,
        occurrenceId: String?,
        mealType: MealType,
        startTimestamp: Long,
        endTimestamp: Long?,
        notes: String?,
        calories: Int?,
        proteinGrams: Double?,
        carbsGrams: Double?,
        fatGrams: Double?,
        sodiumMg: Double?,
        cholesterolMg: Double?,
        fiberGrams: Double?
    ): Long {
        Log.d(
            "MEAL_RECON",
            "repo save mealId=$mealId occurrenceId=$occurrenceId mealType=$mealType"
        )

        val entity = MealLogEntity(
            mealId = mealId,
            occurrenceId = occurrenceId,
            mealType = mealType,
            startTimestamp = startTimestamp,
            endTimestamp = endTimestamp,
            notes = notes,
            calories = calories,
            proteinGrams = proteinGrams,
            carbsGrams = carbsGrams,
            fatGrams = fatGrams,
            sodiumMg = sodiumMg,
            cholesterolMg = cholesterolMg,
            fiberGrams = fiberGrams
        )

        return if (occurrenceId.isNullOrBlank()) {
            dao.insertMealLog(entity)
        } else {
            dao.upsertMealLogByOccurrenceId(entity)
        }
    }
}