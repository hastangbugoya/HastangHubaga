package com.example.hastanghubaga.data.local.dao.meal

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.hastanghubaga.data.local.entity.meal.MealLogEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for actual logged meals.
 *
 * Canonical meal model:
 * - template = MealEntity
 * - occurrence = MealOccurrenceEntity
 * - log = MealLogEntity
 *
 * Timeline reconciliation rule:
 * - planned meal rows come from occurrences
 * - actual meal rows come from logs
 * - if a log links to an occurrenceId, that planned occurrence is considered fulfilled
 *
 * Single-log-per-occurrence rule:
 * - a non-null occurrenceId represents one specific planned occurrence
 * - at most one log row should exist for that occurrence
 * - relogging the same occurrence should update the existing row rather than insert a duplicate
 * - null occurrenceId remains valid for ad-hoc / force-logged meals
 */
@Dao
interface MealLogDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMealLog(log: MealLogEntity): Long

    @Update
    suspend fun updateMealLog(entity: MealLogEntity)

    @Query(
        """
        SELECT *
        FROM meal_logs
        WHERE occurrenceId = :occurrenceId
        LIMIT 1
        """
    )
    suspend fun getMealLogByOccurrenceId(occurrenceId: String): MealLogEntity?

    @Transaction
    suspend fun upsertMealLogByOccurrenceId(log: MealLogEntity): Long {
        val occurrenceId = log.occurrenceId
        if (occurrenceId.isNullOrBlank()) {
            return insertMealLog(log)
        }

        val existing = getMealLogByOccurrenceId(occurrenceId)
        return if (existing == null) {
            insertMealLog(log)
        } else {
            updateMealLog(log.copy(id = existing.id))
            existing.id
        }
    }

    @Query(
        """
        SELECT *
        FROM meal_logs
        WHERE startTimestamp >= :startUtcMillis
          AND startTimestamp < :endUtcMillis
        ORDER BY startTimestamp ASC
        """
    )
    fun observeMealLogsForDay(
        startUtcMillis: Long,
        endUtcMillis: Long
    ): Flow<List<MealLogEntity>>

    @Query(
        """
        SELECT occurrenceId
        FROM meal_logs
        WHERE occurrenceId IS NOT NULL
          AND startTimestamp >= :startUtcMillis
          AND startTimestamp < :endUtcMillis
        """
    )
    suspend fun getSatisfiedOccurrenceIdsForDay(
        startUtcMillis: Long,
        endUtcMillis: Long
    ): List<String>

    @Query("SELECT * FROM meal_logs WHERE id = :id LIMIT 1")
    suspend fun getMealLogById(id: Long): MealLogEntity?
}