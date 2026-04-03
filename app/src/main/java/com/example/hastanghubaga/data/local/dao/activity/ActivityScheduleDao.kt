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

@Dao
interface ActivityScheduleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: ActivityScheduleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedules(schedules: List<ActivityScheduleEntity>): List<Long>

    @Query("""
        SELECT * FROM activity_schedules
        ORDER BY activityId ASC, id ASC
    """)
    fun observeAllSchedules(): Flow<List<ActivityScheduleEntity>>

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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFixedTime(fixedTime: ActivityScheduleFixedTimeEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFixedTimes(fixedTimes: List<ActivityScheduleFixedTimeEntity>): List<Long>

    @Query("""
        SELECT * FROM activity_schedule_fixed_times
        ORDER BY scheduleId ASC, sortOrder ASC, id ASC
    """)
    fun observeAllFixedTimes(): Flow<List<ActivityScheduleFixedTimeEntity>>

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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnchoredTime(anchoredTime: ActivityScheduleAnchoredTimeEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnchoredTimes(
        anchoredTimes: List<ActivityScheduleAnchoredTimeEntity>
    ): List<Long>

    @Query("""
        SELECT * FROM activity_schedule_anchored_times
        ORDER BY scheduleId ASC, sortOrder ASC, id ASC
    """)
    fun observeAllAnchoredTimes(): Flow<List<ActivityScheduleAnchoredTimeEntity>>

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

data class ActivityScheduleWriteModel(
    val schedule: ActivityScheduleEntity,
    val fixedTimes: List<ActivityScheduleFixedTimeEntity> = emptyList(),
    val anchoredTimes: List<ActivityScheduleAnchoredTimeEntity> = emptyList()
)