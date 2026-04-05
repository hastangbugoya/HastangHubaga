package com.example.hastanghubaga.data.local.entity.meal

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents one concrete meal planner occurrence for a specific day/time.
 *
 * This sits between:
 * - reusable meal templates [MealEntity]
 * - persisted meal schedule definitions [MealScheduleEntity]
 * - future historical meal logs [MealLogEntity]
 *
 * Why this exists:
 * - A meal template may occur multiple times per day
 * - A meal schedule may later be edited, re-anchored, or replaced
 * - An extra/manual meal should become a first-class timeline/planner item,
 *   not just a floating log row
 *
 * ## Source types
 * - [MealOccurrenceSourceType.SCHEDULED]
 *   Produced from a persisted meal schedule definition.
 *
 * - [MealOccurrenceSourceType.AD_HOC]
 *   Created on demand when the user adds an extra/manual meal occurrence
 *   that did not originate from an existing planned occurrence.
 *
 * ## Identity
 * [id] is a stable occurrence identifier (UUID/string key).
 * Future meal logs can link to this ID so the app can reconcile:
 * - planner row ↔ logged meal
 * - multiple daily meal occurrences
 * - future edits to schedule definitions without losing historical linkage
 *
 * ## Meal snapshot intent
 * The occurrence is the day-level planned truth used by timeline/planner flows.
 *
 * Canonical rule:
 * - MealEntity holds the reusable template defaults
 * - planned occurrence materialization creates a concrete row for one day/time
 * - future occurrence-level overrides should happen here, not by mutating the
 *   underlying template for one-off daily behavior
 *
 * ## Notes
 * - [scheduleId] is nullable because ad-hoc occurrences are not backed by a
 *   recurring schedule rule.
 * - [isDeleted] supports soft-hiding/canceling an occurrence without removing
 *   linked historical data.
 */
@Entity(
    tableName = "meal_occurrences",
    foreignKeys = [
        ForeignKey(
            entity = MealEntity::class,
            parentColumns = ["id"],
            childColumns = ["mealId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = MealScheduleEntity::class,
            parentColumns = ["id"],
            childColumns = ["scheduleId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("mealId"),
        Index("scheduleId"),
        Index("date"),
        Index(value = ["mealId", "date", "plannedTimeSeconds"]),
        Index(value = ["date", "plannedTimeSeconds"])
    ]
)
data class MealOccurrenceEntity(
    @PrimaryKey
    val id: String,

    val mealId: Long,
    val scheduleId: Long? = null,

    val date: String,
    val plannedTimeSeconds: Int,

    val sourceType: MealOccurrenceSourceType,

    @ColumnInfo(defaultValue = "0")
    val isDeleted: Boolean = false
)

enum class MealOccurrenceSourceType {
    SCHEDULED,
    AD_HOC
}