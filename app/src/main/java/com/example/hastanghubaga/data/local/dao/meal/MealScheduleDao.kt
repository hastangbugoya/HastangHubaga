package com.example.hastanghubaga.data.local.dao.meal

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.hastanghubaga.data.local.entity.meal.MealScheduleAnchoredTimeEntity
import com.example.hastanghubaga.data.local.entity.meal.MealScheduleEntity
import com.example.hastanghubaga.data.local.entity.meal.MealScheduleFixedTimeEntity
import com.example.hastanghubaga.data.local.entity.meal.MealScheduleWithTimes
import kotlinx.coroutines.flow.Flow

/**
 * Room DAO for persisted meal scheduling rules.
 *
 * This mirrors the activity scheduling DAO shape:
 *
 * - one parent schedule row per meal
 * - child rows for fixed times or anchored times
 * - replace-child-list style writes for editor saves
 *
 * ## Design rules
 *
 * - A meal has at most one schedule row
 * - Saving a schedule is done transactionally
 * - Child rows are replaced wholesale on save
 * - Reads expose the fully joined schedule graph so repository/domain/UI can
 *   consume meals exactly like activities
 *
 * ## Child row policy
 *
 * Only one timing mode should be considered active at a time:
 *
 * - FIXED_TIMES -> fixed child rows populated, anchored child rows cleared
 * - ANCHORED -> anchored child rows populated, fixed child rows cleared
 *
 * This DAO intentionally provides low-level primitives plus transactional
 * helpers for the common save flow.
 */
@Dao
interface MealScheduleDao {

    // ------------------------------------------------------------------------
    // Reads
    // ------------------------------------------------------------------------

    @Transaction
    @Query("SELECT * FROM meal_schedules WHERE mealId = :mealId LIMIT 1")
    suspend fun getScheduleForMeal(mealId: Long): MealScheduleWithTimes?

    @Transaction
    @Query("SELECT * FROM meal_schedules WHERE mealId = :mealId LIMIT 1")
    fun observeScheduleForMeal(mealId: Long): Flow<MealScheduleWithTimes?>

    @Transaction
    @Query("SELECT * FROM meal_schedules")
    suspend fun getAllSchedules(): List<MealScheduleWithTimes>

    @Transaction
    @Query("SELECT * FROM meal_schedules")
    fun observeAllSchedules(): Flow<List<MealScheduleWithTimes>>

    // ------------------------------------------------------------------------
    // Parent schedule row writes
    // ------------------------------------------------------------------------

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSchedule(schedule: MealScheduleEntity): Long

    @Query("DELETE FROM meal_schedules WHERE mealId = :mealId")
    suspend fun deleteScheduleForMeal(mealId: Long)

    @Query("SELECT id FROM meal_schedules WHERE mealId = :mealId LIMIT 1")
    suspend fun getScheduleIdForMeal(mealId: Long): Long?

    // ------------------------------------------------------------------------
    // Fixed-time child writes
    // ------------------------------------------------------------------------

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFixedTimes(rows: List<MealScheduleFixedTimeEntity>)

    @Query("DELETE FROM meal_schedule_fixed_times WHERE scheduleId = :scheduleId")
    suspend fun deleteFixedTimesForSchedule(scheduleId: Long)

    // ------------------------------------------------------------------------
    // Anchored-time child writes
    // ------------------------------------------------------------------------

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnchoredTimes(rows: List<MealScheduleAnchoredTimeEntity>)

    @Query("DELETE FROM meal_schedule_anchored_times WHERE scheduleId = :scheduleId")
    suspend fun deleteAnchoredTimesForSchedule(scheduleId: Long)

    // ------------------------------------------------------------------------
    // Transactional helpers
    // ------------------------------------------------------------------------

    /**
     * Replaces the schedule graph for a meal using FIXED_TIMES timing.
     *
     * Behavior:
     * - upsert parent schedule row
     * - clear anchored children
     * - replace fixed children
     */
    @Transaction
    suspend fun replaceWithFixedTimes(
        schedule: MealScheduleEntity,
        fixedTimes: List<MealScheduleFixedTimeEntity>
    ): Long {
        val scheduleId = upsertSchedule(schedule)
        deleteAnchoredTimesForSchedule(scheduleId)
        deleteFixedTimesForSchedule(scheduleId)

        if (fixedTimes.isNotEmpty()) {
            insertFixedTimes(
                fixedTimes.map { it.copy(scheduleId = scheduleId) }
            )
        }

        return scheduleId
    }

    /**
     * Replaces the schedule graph for a meal using ANCHORED timing.
     *
     * Behavior:
     * - upsert parent schedule row
     * - clear fixed-time children
     * - replace anchored children
     */
    @Transaction
    suspend fun replaceWithAnchoredTimes(
        schedule: MealScheduleEntity,
        anchoredTimes: List<MealScheduleAnchoredTimeEntity>
    ): Long {
        val scheduleId = upsertSchedule(schedule)
        deleteFixedTimesForSchedule(scheduleId)
        deleteAnchoredTimesForSchedule(scheduleId)

        if (anchoredTimes.isNotEmpty()) {
            insertAnchoredTimes(
                anchoredTimes.map { it.copy(scheduleId = scheduleId) }
            )
        }

        return scheduleId
    }

    /**
     * Removes the full schedule graph for a meal.
     *
     * Deleting the parent row would cascade to child rows, but this helper keeps
     * the write path explicit and mirrors the style commonly used in the app.
     */
    @Transaction
    suspend fun deleteFullScheduleForMeal(mealId: Long) {
        val scheduleId = getScheduleIdForMeal(mealId)
        if (scheduleId != null) {
            deleteFixedTimesForSchedule(scheduleId)
            deleteAnchoredTimesForSchedule(scheduleId)
        }
        deleteScheduleForMeal(mealId)
    }

    @Query("SELECT * FROM meal_schedules WHERE mealId = :mealId")
    suspend fun getSchedulesForMeal(mealId: Long): List<MealScheduleEntity>

    @Query("""
        SELECT * FROM meal_schedule_fixed_times
        WHERE scheduleId = :scheduleId
        ORDER BY time ASC, id ASC
    """)
    suspend fun getFixedTimesForSchedule(scheduleId: Long): List<MealScheduleFixedTimeEntity>

    @Query("""
        SELECT * FROM meal_schedule_anchored_times
        WHERE scheduleId = :scheduleId
        ORDER BY id ASC
    """)
    suspend fun getAnchoredTimesForSchedule(scheduleId: Long): List<MealScheduleAnchoredTimeEntity>
}