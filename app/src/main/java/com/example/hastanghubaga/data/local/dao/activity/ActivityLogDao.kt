package com.example.hastanghubaga.data.local.dao.activity

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.hastanghubaga.data.local.entity.activity.ActivityLogEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for actual logged activity sessions.
 *
 * Canonical activity model:
 * - template = ActivityEntity
 * - occurrence = ActivityOccurrenceEntity
 * - log = ActivityLogEntity
 *
 * Timeline reconciliation rule:
 * - planned activity rows come from occurrences
 * - actual activity rows come from logs
 * - if a log links to an occurrenceId, that planned occurrence is considered fulfilled
 */
@Dao
interface ActivityLogDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertActivityLog(log: ActivityLogEntity): Long

    @Update
    suspend fun updateActivityLog(entity: ActivityLogEntity)

    @Query(
        """
        SELECT * 
        FROM activity_logs
        WHERE startTimestamp >= :startUtcMillis
          AND startTimestamp < :endUtcMillis
        ORDER BY startTimestamp ASC
        """
    )
    fun observeActivityLogsForDay(
        startUtcMillis: Long,
        endUtcMillis: Long
    ): Flow<List<ActivityLogEntity>>

    @Query(
        """
        SELECT occurrenceId
        FROM activity_logs
        WHERE occurrenceId IS NOT NULL
          AND startTimestamp >= :startUtcMillis
          AND startTimestamp < :endUtcMillis
        """
    )
    suspend fun getSatisfiedOccurrenceIdsForDay(
        startUtcMillis: Long,
        endUtcMillis: Long
    ): List<String>

    @Query("SELECT * FROM activity_logs WHERE id = :id LIMIT 1")
    suspend fun getActivityLogById(id: Long): ActivityLogEntity?
}