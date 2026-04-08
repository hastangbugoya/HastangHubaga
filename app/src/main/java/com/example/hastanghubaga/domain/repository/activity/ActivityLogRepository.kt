package com.example.hastanghubaga.domain.repository.activity

import com.example.hastanghubaga.domain.model.activity.ActivityLog
import com.example.hastanghubaga.domain.model.activity.ActivityType
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
 *
 * Single-log-per-occurrence rule:
 * - a non-null occurrenceId identifies one specific planned occurrence
 * - at most one persisted log row may exist for that occurrence
 * - saving the same non-null occurrenceId again must update/replace the existing
 *   log row rather than create a duplicate row
 * - null occurrenceId remains valid for ad-hoc / force-logged activity logs
 */
interface ActivityLogRepository {

    /**
     * Observes actual logged activity sessions whose start timestamp falls on the
     * supplied local date.
     */
    fun observeActivityLogsForDate(date: LocalDate): Flow<List<ActivityLog>>

    /**
     * Persists one actual logged activity session.
     *
     * Persistence semantics:
     * - if [occurrenceId] is non-null, this must behave as upsert-by-occurrenceId
     * - if [occurrenceId] is null, this behaves as a normal insert for an ad-hoc log
     *
     * Snapshot rule:
     * - [title] is the user-facing display name snapshot for the logged activity
     * - [activityType] remains category-only metadata
     * - normal planned logging should copy title/location from the occurrence layer
     *
     * Location snapshot rule:
     * - savedAddressId/addressAsRawString represent the actual location snapshot
     *   for the logged activity
     * - addressDisplayText is the UI-ready display snapshot
     * - normal planned logging should copy these from the occurrence layer
     *
     * Returns:
     * - inserted row id for a new row
     * - existing row id when an existing planned occurrence log is updated
     */
    suspend fun insertActivityLog(
        activityId: Long?,
        occurrenceId: String?,
        title: String,
        activityType: ActivityType,
        startTimestamp: Long,
        endTimestamp: Long?,
        notes: String?,
        intensity: Int?,
        savedAddressId: Long?,
        addressAsRawString: String?,
        addressDisplayText: String?
    ): Long
}