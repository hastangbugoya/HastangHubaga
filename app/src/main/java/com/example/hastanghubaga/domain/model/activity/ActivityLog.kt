package com.example.hastanghubaga.domain.model.activity

import kotlinx.datetime.LocalDateTime

/**
 * Domain representation of one actual logged activity session.
 *
 * Canonical activity model:
 * - ActivityEntity = reusable template / definition
 * - ActivityOccurrenceEntity = planned occurrence for a date/time
 * - ActivityLogEntity = actual performed session
 *
 * This model represents reality, not planner intent.
 *
 * Reconciliation contract:
 * - if [occurrenceId] is non-null, this log fulfills that planned occurrence
 * - timeline builders may suppress the matching planned card and show this log
 * - if [occurrenceId] is null, this is an extra / unplanned activity log
 *
 * Snapshot rule:
 * - [title] is stored on the log so history remains stable even if the
 *   template or occurrence title later changes
 * - [activityType] is stored on the log so history remains stable even if the
 *   template later changes
 * - address fields are stored on the log so expanded timeline/history views
 *   remain stable even if the template or saved address later changes
 */
data class ActivityLog(
    val id: Long,

    /**
     * Optional reference to the template activity this log came from.
     *
     * Nullable for forward flexibility and extra/unplanned logging cases.
     */
    val activityId: Long? = null,

    /**
     * Optional planned occurrence fulfilled by this actual log.
     *
     * - non-null = planned activity was logged
     * - null = extra / unplanned activity log
     */
    val occurrenceId: String? = null,

    /**
     * Snapshot of the user-facing activity title at log time.
     */
    val title: String,

    /**
     * Snapshot of the activity type at log time.
     */
    val activityType: ActivityType,

    /**
     * Actual performed start datetime in local/domain time.
     */
    val start: LocalDateTime,

    /**
     * Actual performed end datetime in local/domain time.
     *
     * Nullable for forward compatibility while the logging model stays minimal.
     */
    val end: LocalDateTime? = null,

    /**
     * Optional user-entered note for this actual session.
     */
    val notes: String? = null,

    /**
     * Optional user-reported effort level.
     *
     * Current convention:
     * - 1 = very light
     * - 5 = moderate
     * - 10 = maximal effort
     */
    val intensity: Int? = null,

    /**
     * Optional saved/favorite address referenced at log time.
     *
     * Nullable because:
     * - many activities do not have a location
     * - the user may log using only raw text
     * - historical logs should still exist even if the saved address is later deleted
     */
    val savedAddressId: Long? = null,

    /**
     * Optional raw address text snapshot stored on the log.
     *
     * This preserves user-entered freeform location text even when there is no
     * saved address row.
     */
    val addressAsRawString: String? = null,

    /**
     * Optional display-ready address snapshot stored on the log.
     *
     * Timeline/history UI should prefer this over [addressAsRawString] when present.
     */
    val addressDisplayText: String? = null
)