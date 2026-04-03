package com.example.hastanghubaga.data.local.dao.activity

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.hastanghubaga.data.local.entity.activity.ActivityScheduleAnchoredTimeEntity
import com.example.hastanghubaga.data.local.entity.activity.ActivityScheduleEntity
import com.example.hastanghubaga.data.local.entity.activity.ActivityScheduleFixedTimeEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for persisted activity schedules.
 *
 * This DAO stores the actual planning/scheduling rules for activities.
 * It is intentionally separate from ActivityEntity, which may continue to
 * represent the reusable activity definition/template.
 */
@Dao
interface ActivityScheduleDao {

    // -------------------------
    // Parent schedule rows
    // -------------------------
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: ActivityScheduleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedules(schedules: List<ActivityScheduleEntity>): List<Long>

    @Query("""
        SELECT * FROM activity_schedules
        WHERE activityId = :activityId
        ORDER BY id ASC
    """)
    suspend fun getSchedulesForActivity(activityId: Long): List<ActivityScheduleEntity>

    @Query("""
        SELECT * FROM activity_schedules
        WHERE activityId = :activityId
        ORDER BY id ASC
    """)
    fun observeSchedulesForActivity(activityId: Long): Flow<List<ActivityScheduleEntity>>

    @Query("""
        DELETE FROM activity_schedules
        WHERE activityId = :activityId
    """)
    suspend fun deleteSchedulesForActivity(activityId: Long)

    // -------------------------
    // Fixed child rows
    // -------------------------
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFixedTime(fixedTime: ActivityScheduleFixedTimeEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFixedTimes(fixedTimes: List<ActivityScheduleFixedTimeEntity>): List<Long>

    @Query("""
        SELECT * FROM activity_schedule_fixed_times
        WHERE scheduleId = :scheduleId
        ORDER BY sortOrder ASC, id ASC
    """)
    suspend fun getFixedTimesForSchedule(scheduleId: Long): List<ActivityScheduleFixedTimeEntity>

    @Query("""
        DELETE FROM activity_schedule_fixed_times
        WHERE scheduleId = :scheduleId
    """)
    suspend fun deleteFixedTimesForSchedule(scheduleId: Long)

    // -------------------------
    // Anchored child rows
    // -------------------------
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnchoredTime(anchoredTime: ActivityScheduleAnchoredTimeEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnchoredTimes(
        anchoredTimes: List<ActivityScheduleAnchoredTimeEntity>
    ): List<Long>

    @Query("""
        SELECT * FROM activity_schedule_anchored_times
        WHERE scheduleId = :scheduleId
        ORDER BY sortOrder ASC, id ASC
    """)
    suspend fun getAnchoredTimesForSchedule(scheduleId: Long): List<ActivityScheduleAnchoredTimeEntity>

    @Query("""
        DELETE FROM activity_schedule_anchored_times
        WHERE scheduleId = :scheduleId
    """)
    suspend fun deleteAnchoredTimesForSchedule(scheduleId: Long)

    // -------------------------
    // Transactional replace/upsert
    // -------------------------
    /**
     * Replaces the full persisted schedule set for one activity.
     *
     * This mirrors the supplement schedule save path:
     * - user edits the full schedule collection in memory
     * - save replaces persisted schedules atomically
     */
    @Transaction
    suspend fun replaceSchedulesForActivity(
        activityId: Long,
        schedules: List<ActivityScheduleWriteModel>
    ) {
        deleteSchedulesForActivity(activityId)

        schedules.forEach { model ->
            val newScheduleId = insertSchedule(
                model.schedule.copy(
                    id = 0L,
                    activityId = activityId
                )
            )

            val fixedTimes = model.fixedTimes.map { fixed ->
                fixed.copy(
                    id = 0L,
                    scheduleId = newScheduleId
                )
            }

            if (fixedTimes.isNotEmpty()) {
                insertFixedTimes(fixedTimes)
            }

            val anchoredTimes = model.anchoredTimes.map { anchored ->
                anchored.copy(
                    id = 0L,
                    scheduleId = newScheduleId
                )
            }

            if (anchoredTimes.isNotEmpty()) {
                insertAnchoredTimes(anchoredTimes)
            }
        }
    }
}

/**
 * Write model used by the editor save path.
 *
 * One parent schedule row plus its child occurrence rows.
 * Exactly one of fixedTimes or anchoredTimes should normally be non-empty,
 * matching the parent timingType.
 */
data class ActivityScheduleWriteModel(
    val schedule: ActivityScheduleEntity,
    val fixedTimes: List<ActivityScheduleFixedTimeEntity> = emptyList(),
    val anchoredTimes: List<ActivityScheduleAnchoredTimeEntity> = emptyList()
)