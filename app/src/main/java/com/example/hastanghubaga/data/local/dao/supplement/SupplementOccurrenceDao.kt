package com.example.hastanghubaga.data.local.dao.supplement

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.hastanghubaga.data.local.entity.supplement.SupplementOccurrenceEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for concrete supplement planner occurrences.
 *
 * This table sits below reusable schedule definitions and above historical logs.
 *
 * Responsibilities:
 * - Persist concrete supplement occurrences for a specific day/time
 * - Support occurrence-aware reconciliation between planner rows and logs
 * - Allow ad-hoc / extra supplement doses to become first-class timeline items
 *
 * Typical usage:
 * - Scheduled occurrence: created from a recurring supplement schedule
 * - Ad-hoc occurrence: created when the user logs an extra/manual dose
 *
 * Notes:
 * - Occurrences are intentionally separate from [SupplementScheduleDao]
 *   because schedules are reusable rules, while occurrences are concrete instances.
 * - Logs may reference occurrences by stable ID.
 */
@Dao
interface SupplementOccurrenceDao {

    /**
     * Inserts or replaces a single supplement occurrence.
     *
     * REPLACE is acceptable here because occurrence IDs are stable identifiers
     * and callers may intentionally upsert the same occurrence.
     *
     * @return the inserted row ID
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOccurrence(occurrence: SupplementOccurrenceEntity): Long

    /**
     * Inserts or replaces multiple supplement occurrences.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOccurrences(
        occurrences: List<SupplementOccurrenceEntity>
    ): List<Long>

    /**
     * Returns a single occurrence by its stable occurrence ID.
     */
    @Query(
        """
        SELECT * FROM supplement_occurrences
        WHERE id = :occurrenceId
        LIMIT 1
        """
    )
    suspend fun getOccurrenceById(
        occurrenceId: String
    ): SupplementOccurrenceEntity?

    /**
     * Observes all non-deleted supplement occurrences for a specific date.
     *
     * Ordered chronologically for timeline-style consumers.
     */
    @Query(
        """
        SELECT * FROM supplement_occurrences
        WHERE date = :date
          AND isDeleted = 0
        ORDER BY plannedTimeSeconds ASC, id ASC
        """
    )
    fun observeOccurrencesForDate(
        date: String
    ): Flow<List<SupplementOccurrenceEntity>>

    /**
     * Non-reactive read of all non-deleted supplement occurrences for a specific date.
     *
     * Ordered chronologically for timeline-style consumers.
     */
    @Query(
        """
        SELECT * FROM supplement_occurrences
        WHERE date = :date
          AND isDeleted = 0
        ORDER BY plannedTimeSeconds ASC, id ASC
        """
    )
    suspend fun getOccurrencesForDate(
        date: String
    ): List<SupplementOccurrenceEntity>

    /**
     * Returns all non-deleted occurrences for one supplement on one date.
     *
     * Useful for reconciliation and duplicate-prevention checks.
     */
    @Query(
        """
        SELECT * FROM supplement_occurrences
        WHERE supplementId = :supplementId
          AND date = :date
          AND isDeleted = 0
        ORDER BY plannedTimeSeconds ASC, id ASC
        """
    )
    suspend fun getOccurrencesForSupplementOnDate(
        supplementId: Long,
        date: String
    ): List<SupplementOccurrenceEntity>

    /**
     * Soft-deletes a single occurrence.
     *
     * This preserves historical linkage for logs while removing the planner item
     * from active timeline queries.
     */
    @Query(
        """
        UPDATE supplement_occurrences
        SET isDeleted = 1
        WHERE id = :occurrenceId
        """
    )
    suspend fun softDeleteOccurrence(
        occurrenceId: String
    )

    /**
     * Soft-deletes all occurrences for a supplement on a given date.
     *
     * Useful for rebuild/reset workflows.
     */
    @Query(
        """
        UPDATE supplement_occurrences
        SET isDeleted = 1
        WHERE supplementId = :supplementId
          AND date = :date
        """
    )
    suspend fun softDeleteOccurrencesForSupplementOnDate(
        supplementId: Long,
        date: String
    )

    /**
     * Hard-deletes all occurrences for a specific date.
     *
     * Use carefully. Prefer soft-delete when preserving historical relationships matters.
     */
    @Query(
        """
        DELETE FROM supplement_occurrences
        WHERE date = :date
        """
    )
    suspend fun deleteOccurrencesForDate(
        date: String
    )
}