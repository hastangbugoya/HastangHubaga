package com.example.hastanghubaga.data.local.entity.activity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents one concrete activity planner occurrence for a specific day/time.
 *
 * This sits between:
 * - reusable schedule definitions [ActivityScheduleEntity]
 * - historical activity logs [ActivityLogEntity] (future)
 *
 * Why this exists:
 * - An activity may occur multiple times per day
 * - An activity schedule may later be edited, re-anchored, or replaced
 * - An extra/manual activity should become a first-class timeline/planner item,
 *   not just a floating log row
 *
 * ## Source types
 * - [ActivityOccurrenceSourceType.SCHEDULED]
 *   Produced from a persisted activity schedule definition.
 *
 * - [ActivityOccurrenceSourceType.AD_HOC]
 *   Created on demand when the user adds an extra/manual activity occurrence
 *   that did not originate from an existing planned occurrence.
 *
 * ## Identity
 * [id] is a stable occurrence identifier (UUID/string key).
 * Future logs can link to this ID so the app can reconcile:
 * - planner row ↔ logged activity
 * - multiple daily occurrences
 * - future edits to schedule definitions without losing historical linkage
 *
 * ## Workout flag
 * [isWorkout] is the occurrence-level snapshot used by the day planner/timeline.
 *
 * Canonical rule:
 * - ActivityEntity.isWorkout is the template default
 * - planned occurrence materialization copies that default into [isWorkout]
 * - the user may later toggle [isWorkout] for a specific occurrence without
 *   mutating the underlying template
 *
 * This keeps the day-level anchor system deterministic:
 * - the planner reads the occurrence snapshot
 * - not the template directly
 *
 * ## Notes
 * - [scheduleId] is nullable because ad-hoc occurrences are not backed by a
 *   recurring schedule rule.
 * - [isDeleted] supports soft-hiding/canceling an occurrence without removing
 *   linked historical data.
 */
@Entity(
    tableName = "activity_occurrences",
    foreignKeys = [
        ForeignKey(
            entity = ActivityEntity::class,
            parentColumns = ["id"],
            childColumns = ["activityId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ActivityScheduleEntity::class,
            parentColumns = ["id"],
            childColumns = ["scheduleId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("activityId"),
        Index("scheduleId"),
        Index("date"),
        Index(value = ["activityId", "date", "plannedTimeSeconds"]),
        Index(value = ["date", "isWorkout", "plannedTimeSeconds"])
    ]
)
data class ActivityOccurrenceEntity(
    @PrimaryKey
    val id: String,

    val activityId: Long,
    val scheduleId: Long? = null,

    val date: String,
    val plannedTimeSeconds: Int,

    val sourceType: ActivityOccurrenceSourceType,

    @ColumnInfo(defaultValue = "0")
    val isDeleted: Boolean = false,

    @ColumnInfo(defaultValue = "0")
    val isWorkout: Boolean = false
)

enum class ActivityOccurrenceSourceType {
    SCHEDULED,
    AD_HOC
}