package com.example.hastanghubaga.data.local.entity.activity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.hastanghubaga.domain.model.activity.ActivityType

/**
 * Represents one actual logged activity session performed by the user.
 *
 * Canonical activity model:
 * - [ActivityEntity] = reusable template / definition
 * - [ActivityOccurrenceEntity] = planned occurrence for a specific day/time
 * - [ActivityLogEntity] = actual performed session
 *
 * Reconciliation contract:
 * - if [occurrenceId] is non-null, this log fulfills that planned occurrence
 * - timeline builders may suppress the matching planned card and show only the log
 * - if [occurrenceId] is null, this is an extra / unplanned activity log
 *
 * Snapshot rule:
 * - [activityType] is copied into the log so history remains stable even if the
 *   template later changes
 *
 * Single-log-per-occurrence rule:
 * - [occurrenceId] is the stable key for a planned activity occurrence
 * - at most one persisted log row may exist for a given non-null [occurrenceId]
 * - null [occurrenceId] values remain allowed for ad-hoc / force-logged activities
 *
 * Minimal v1 scope:
 * - actual start/end timestamps
 * - optional notes
 * - optional intensity
 *
 * Future fields can be added later as needed.
 */
@Entity(
    tableName = "activity_logs",
    foreignKeys = [
        ForeignKey(
            entity = ActivityEntity::class,
            parentColumns = ["id"],
            childColumns = ["activityId"],
            onDelete = ForeignKey.SET_NULL
        ),
    ],
    indices = [
        Index("activityId"),
        Index(value = ["occurrenceId"], unique = true),
        Index("startTimestamp")
    ]
)
data class ActivityLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    /**
     * Optional template reference.
     *
     * - non-null when this log came from a known activity template
     * - null is allowed for future flexibility, though normal planned logging
     *   should usually preserve the template link
     */
    val activityId: Long? = null,

    /**
     * Optional planned occurrence fulfilled by this actual log.
     *
     * - non-null = planned activity log
     * - null = extra / unplanned activity log
     *
     * This value is the canonical reconciliation key for planned logging.
     * Only one persisted log row may exist for a given non-null occurrenceId.
     */
    val occurrenceId: String? = null,

    /**
     * Snapshot of the activity type at log time.
     *
     * This preserves historical correctness even if the template changes later.
     */
    val activityType: ActivityType,

    /**
     * Actual performed start time in UTC epoch millis.
     */
    val startTimestamp: Long,

    /**
     * Actual performed end time in UTC epoch millis.
     *
     * Nullable for forward compatibility while the logging model is still simple.
     */
    val endTimestamp: Long? = null,

    /**
     * Optional freeform log note.
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
    val intensity: Int? = null
)