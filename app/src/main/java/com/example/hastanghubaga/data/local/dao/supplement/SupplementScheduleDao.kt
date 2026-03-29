package com.example.hastanghubaga.data.local.dao.supplement

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.hastanghubaga.data.local.entity.supplement.SupplementScheduleAnchoredTimeEntity
import com.example.hastanghubaga.data.local.entity.supplement.SupplementScheduleEntity
import com.example.hastanghubaga.data.local.entity.supplement.SupplementScheduleFixedTimeEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for persisted supplement schedules.
 *
 * This DAO stores the actual planning/scheduling rules for supplements.
 * It is intentionally separate from SupplementEntity, which may still carry
 * user-facing recommendation metadata.
 */
@Dao
interface SupplementScheduleDao {

    // -------------------------
    // Parent schedule rows
    // -------------------------
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: SupplementScheduleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedules(schedules: List<SupplementScheduleEntity>): List<Long>

    @Query("""
        SELECT * FROM supplement_schedules
        WHERE supplementId = :supplementId
        ORDER BY id ASC
    """)
    suspend fun getSchedulesForSupplement(supplementId: Long): List<SupplementScheduleEntity>

    @Query("""
        SELECT * FROM supplement_schedules
        WHERE supplementId = :supplementId
        ORDER BY id ASC
    """)
    fun observeSchedulesForSupplement(supplementId: Long): Flow<List<SupplementScheduleEntity>>

    @Query("""
        DELETE FROM supplement_schedules
        WHERE supplementId = :supplementId
    """)
    suspend fun deleteSchedulesForSupplement(supplementId: Long)

    // -------------------------
    // Fixed child rows
    // -------------------------
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFixedTime(fixedTime: SupplementScheduleFixedTimeEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFixedTimes(fixedTimes: List<SupplementScheduleFixedTimeEntity>): List<Long>

    @Query("""
        SELECT * FROM supplement_schedule_fixed_times
        WHERE scheduleId = :scheduleId
        ORDER BY sortOrder ASC, id ASC
    """)
    suspend fun getFixedTimesForSchedule(scheduleId: Long): List<SupplementScheduleFixedTimeEntity>

    @Query("""
        DELETE FROM supplement_schedule_fixed_times
        WHERE scheduleId = :scheduleId
    """)
    suspend fun deleteFixedTimesForSchedule(scheduleId: Long)

    // -------------------------
    // Anchored child rows
    // -------------------------
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnchoredTime(anchoredTime: SupplementScheduleAnchoredTimeEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnchoredTimes(
        anchoredTimes: List<SupplementScheduleAnchoredTimeEntity>
    ): List<Long>

    @Query("""
        SELECT * FROM supplement_schedule_anchored_times
        WHERE scheduleId = :scheduleId
        ORDER BY sortOrder ASC, id ASC
    """)
    suspend fun getAnchoredTimesForSchedule(scheduleId: Long): List<SupplementScheduleAnchoredTimeEntity>

    @Query("""
        DELETE FROM supplement_schedule_anchored_times
        WHERE scheduleId = :scheduleId
    """)
    suspend fun deleteAnchoredTimesForSchedule(scheduleId: Long)

    // -------------------------
    // Transactional replace/upsert
    // -------------------------
    /**
     * Replaces the full persisted schedule set for one supplement.
     *
     * This is the easiest and safest write path for the editor:
     * - user edits full schedule collection in memory
     * - save replaces persisted schedules atomically
     */
    @Transaction
    suspend fun replaceSchedulesForSupplement(
        supplementId: Long,
        schedules: List<SupplementScheduleWriteModel>
    ) {
        deleteSchedulesForSupplement(supplementId)

        schedules.forEach { model ->
            val newScheduleId = insertSchedule(
                model.schedule.copy(
                    id = 0L,
                    supplementId = supplementId
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
data class SupplementScheduleWriteModel(
    val schedule: SupplementScheduleEntity,
    val fixedTimes: List<SupplementScheduleFixedTimeEntity> = emptyList(),
    val anchoredTimes: List<SupplementScheduleAnchoredTimeEntity> = emptyList()
)