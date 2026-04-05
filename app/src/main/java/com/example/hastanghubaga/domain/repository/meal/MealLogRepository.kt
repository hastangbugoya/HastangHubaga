package com.example.hastanghubaga.domain.repository.meal

import com.example.hastanghubaga.data.local.entity.meal.MealType
import com.example.hastanghubaga.domain.model.meal.MealLog
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

/**
 * Repository for actual consumed HH meal logs.
 *
 * Canonical meal model:
 * - MealEntity = template
 * - MealOccurrenceEntity = planned occurrence
 * - MealLogEntity = actual consumed meal
 *
 * Reconciliation contract:
 * - planned timeline rows come from occurrences
 * - actual timeline rows come from meal logs
 * - if a log has a non-null occurrenceId, that occurrence is considered fulfilled
 *   and the matching planned card may be suppressed
 *
 * Single-log-per-occurrence rule:
 * - a non-null occurrenceId identifies one specific planned occurrence
 * - at most one persisted log row may exist for that occurrence
 * - saving the same non-null occurrenceId again must update/replace the existing
 *   log row rather than create a duplicate row
 * - null occurrenceId remains valid for ad-hoc / force-logged meal logs
 */
interface MealLogRepository {

    /**
     * Observes actual logged meals whose start timestamp falls on the supplied
     * local date.
     */
    fun observeMealLogsForDate(date: LocalDate): Flow<List<MealLog>>

    /**
     * Persists one actual logged meal.
     *
     * Persistence semantics:
     * - if [occurrenceId] is non-null, this must behave as upsert-by-occurrenceId
     * - if [occurrenceId] is null, this behaves as a normal insert for an ad-hoc log
     *
     * Returns:
     * - inserted row id for a new row
     * - existing row id when an existing planned occurrence log is updated
     */
    suspend fun insertMealLog(
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
    ): Long
}