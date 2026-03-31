package com.example.hastanghubaga.data.local.entity.supplement

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents one concrete supplement planner occurrence for a specific day/time.
 *
 * This sits between:
 * - reusable schedule definitions ([SupplementScheduleEntity])
 * - historical intake logs ([SupplementDailyLogEntity])
 *
 * Why this exists:
 * - A supplement may occur multiple times per day
 * - A supplement schedule may later be edited, re-anchored, or replaced
 * - An extra/manual dose should become a first-class timeline/planner item,
 *   not just a floating log row
 *
 * ## Source types
 * - [SupplementOccurrenceSourceType.SCHEDULED]
 *   Produced from a persisted supplement schedule definition.
 *
 * - [SupplementOccurrenceSourceType.AD_HOC]
 *   Created on demand when the user logs an extra/manual dose that did not
 *   originate from an existing planned occurrence.
 *
 * ## Identity
 * [id] is a stable occurrence identifier (UUID string).
 * Logs can link to this ID so the app can reconcile:
 * - planner row ↔ logged dose
 * - multiple daily doses
 * - future edits to schedule definitions without losing historical linkage
 *
 * ## Notes
 * - [scheduleId] is nullable because ad-hoc occurrences are not backed by a
 *   recurring schedule rule.
 * - [isDeleted] supports soft-hiding/canceling an occurrence without removing
 *   linked historical data.
 */
@Entity(
    tableName = "supplement_occurrences",
    foreignKeys = [
        ForeignKey(
            entity = SupplementEntity::class,
            parentColumns = ["id"],
            childColumns = ["supplementId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SupplementScheduleEntity::class,
            parentColumns = ["id"],
            childColumns = ["scheduleId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("supplementId"),
        Index("scheduleId"),
        Index("date"),
        Index(value = ["supplementId", "date", "plannedTimeSeconds"])
    ]
)
data class SupplementOccurrenceEntity(
    @PrimaryKey
    val id: String,

    val supplementId: Long,
    val scheduleId: Long? = null,

    val date: String,
    val plannedTimeSeconds: Int,

    val sourceType: SupplementOccurrenceSourceType,
    val isDeleted: Boolean = false
)

enum class SupplementOccurrenceSourceType {
    SCHEDULED,
    AD_HOC
}