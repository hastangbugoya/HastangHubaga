package com.example.hastanghubaga.data.local.entity.meal

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Persisted scheduling rule for a meal template.
 *
 * This table defines WHETHER a meal is scheduled on a given date and WHICH
 * timing mode it uses. The actual resolved time-of-day is stored in child tables:
 *
 * - fixed times -> meal_schedule_fixed_times
 * - anchored times -> meal_schedule_anchored_times
 *
 * This mirrors the activity scheduling architecture:
 *
 * meal template -> meal schedule -> (future) materialized occurrence -> (future) log -> timeline
 *
 * ## Why this table exists
 *
 * [MealEntity] defines what the meal is.
 * [MealScheduleEntity] defines when the meal is eligible to appear.
 *
 * This separation is important because:
 *
 * - a meal template is reusable across many days
 * - scheduling rules can be changed without redefining the meal itself
 * - future occurrence materialization can use this table as its source of truth
 *
 * ## Recurrence storage
 *
 * To keep this first pass DB-safe and migration-friendly, recurrence/timing fields
 * are stored as strings rather than enums in this table.
 *
 * Expected values:
 *
 * - [recurrenceType]
 *   - "DAILY"
 *   - "EVERY_X_DAYS"
 *   - "WEEKLY"
 *
 * - [timingType]
 *   - "FIXED_TIMES"
 *   - "ANCHORED"
 *
 * - [weeklyDays]
 *   Comma-separated ISO day names only when [recurrenceType] = "WEEKLY".
 *   Example:
 *   "MONDAY,WEDNESDAY,FRIDAY"
 *
 * ## Date storage
 *
 * [startDate] and [endDate] are stored as ISO-8601 date strings:
 *
 * - "2026-04-04"
 *
 * This avoids requiring a Room converter in this first file and keeps the schema
 * easy to inspect during development.
 *
 * ## Hard rules
 *
 * - There should be at most one active schedule row per meal template
 * - Child timing rows must match the selected [timingType]
 * - Disabling a schedule should prevent planned meal rows from being generated
 * - This table does NOT represent a concrete occurrence
 */
@Serializable
@Entity(
    tableName = "meal_schedules",
    foreignKeys = [
        ForeignKey(
            entity = MealEntity::class,
            parentColumns = ["id"],
            childColumns = ["mealId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["mealId"], unique = true)
    ]
)
data class MealScheduleEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    /**
     * Parent meal template id.
     */
    val mealId: Long,

    /**
     * DAILY, EVERY_X_DAYS, WEEKLY
     */
    @ColumnInfo(defaultValue = "DAILY")
    val recurrenceType: String = "DAILY",

    /**
     * Used only for EVERY_X_DAYS.
     * Must be >= 1.
     */
    @ColumnInfo(defaultValue = "1")
    val interval: Int = 1,

    /**
     * Comma-separated ISO weekday names when recurrenceType = WEEKLY.
     * Example: "MONDAY,WEDNESDAY,FRIDAY"
     */
    val weeklyDays: String? = null,

    /**
     * Inclusive ISO start date for the schedule.
     * Example: "2026-04-04"
     */
    val startDate: String,

    /**
     * Optional inclusive ISO end date.
     * Null means open-ended.
     */
    val endDate: String? = null,

    /**
     * FIXED_TIMES or ANCHORED
     */
    @ColumnInfo(defaultValue = "FIXED_TIMES")
    val timingType: String = "FIXED_TIMES",

    /**
     * Whether this schedule is currently enabled.
     */
    @ColumnInfo(defaultValue = "1")
    val isEnabled: Boolean = true
)
