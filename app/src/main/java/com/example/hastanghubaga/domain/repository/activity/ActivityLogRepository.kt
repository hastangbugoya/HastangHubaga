package com.example.hastanghubaga.domain.repository.activity

import com.example.hastanghubaga.domain.model.activity.ActivityLog
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

/**
 * Repository for actual performed activity sessions.
 *
 * Canonical activity model:
 * - ActivityEntity = template
 * - ActivityOccurrenceEntity = planned occurrence
 * - ActivityLogEntity = actual performed session
 *
 * Reconciliation contract:
 * - planned timeline rows come from occurrences
 * - actual timeline rows come from logs
 * - if a log has a non-null occurrenceId, that occurrence is considered fulfilled
 *   and the matching planned card may be suppressed
 */
interface ActivityLogRepository {

    /**
     * Observes actual logged activity sessions whose start timestamp falls on the
     * supplied local date.
     */
    fun observeActivityLogsForDate(date: LocalDate): Flow<List<ActivityLog>>

    /**
     * Inserts one actual logged activity session.
     *
     * Returns the new log row id.
     */
    suspend fun insertActivityLog(
        activityId: Long?,
        occurrenceId: String?,
        activityType: com.example.hastanghubaga.domain.model.activity.ActivityType,
        startTimestamp: Long,
        endTimestamp: Long?,
        notes: String?,
        intensity: Int?
    ): Long
}