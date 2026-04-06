package com.example.hastanghubaga.data.local.dao.activity

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
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
 *
 * Single-log-per-occurrence rule:
 * - a non-null occurrenceId represents one specific planned occurrence
 * - at most one log row should exist for that occurrence
 * - relogging the same occurrence should update the existing row rather than insert a duplicate
 * - null occurrenceId remains valid for ad-hoc / force-logged activities and should insert normally
 */
@Dao
interface ActivityLogDao {

    /**
     * Raw insert for new activity log rows.
     *
     * For planned logs tied to an occurrenceId, prefer [upsertActivityLogByOccurrenceId]
     * so repeated logging of the same planned occurrence updates the existing row.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertActivityLog(log: ActivityLogEntity): Long

    @Update
    suspend fun updateActivityLog(entity: ActivityLogEntity)

    /**
     * Returns the existing log that fulfills the given planned occurrence, if any.
     */
    @Query(
        """
        SELECT *
        FROM activity_logs
        WHERE occurrenceId = :occurrenceId
        LIMIT 1
        """
    )
    suspend fun getActivityLogByOccurrenceId(occurrenceId: String): ActivityLogEntity?

    /**
     * Enforces one persisted log row per non-null occurrenceId.
     *
     * Behavior:
     * - if [log.occurrenceId] is null, inserts a new ad-hoc log row
     * - if [log.occurrenceId] is non-null and no existing row matches, inserts a new row
     * - if [log.occurrenceId] is non-null and an existing row matches, updates that row
     *
     * The existing primary key is preserved during update so UI/state that depends on
     * row identity remains stable.
     *
     * @return the persisted row id:
     * - new inserted row id when inserted
     * - existing row id when updated
     */
    @Transaction
    suspend fun upsertActivityLogByOccurrenceId(log: ActivityLogEntity): Long {
        val occurrenceId = log.occurrenceId
        if (occurrenceId.isNullOrBlank()) {
            return insertActivityLog(log)
        }

        val existing = getActivityLogByOccurrenceId(occurrenceId)
        return if (existing == null) {
            insertActivityLog(log)
        } else {
            updateActivityLog(log.copy(id = existing.id))
            existing.id
        }
    }

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

// --Commented out by Inspection START (4/6/2026 3:19 PM):
//    @Query(
//        """
//        SELECT occurrenceId
//        FROM activity_logs
//        WHERE occurrenceId IS NOT NULL
//          AND startTimestamp >= :startUtcMillis
//          AND startTimestamp < :endUtcMillis
//        """
//    )
//    suspend fun getSatisfiedOccurrenceIdsForDay(
//        startUtcMillis: Long,
//        endUtcMillis: Long
//    ): List<String>
// --Commented out by Inspection STOP (4/6/2026 3:19 PM)

// --Commented out by Inspection START (4/6/2026 3:19 PM):
//    @Query("SELECT * FROM activity_logs WHERE id = :id LIMIT 1")
//    suspend fun getActivityLogById(id: Long): ActivityLogEntity?
// --Commented out by Inspection STOP (4/6/2026 3:19 PM)
}