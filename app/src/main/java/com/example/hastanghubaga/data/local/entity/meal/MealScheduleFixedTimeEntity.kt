package com.example.hastanghubaga.data.local.entity.meal

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Fixed-time child rows for a meal schedule.
 *
 * This table stores one or more concrete local times-of-day for a parent
 * [MealScheduleEntity] whose timing mode is FIXED_TIMES.
 *
 * This mirrors the activity scheduling pattern:
 *
 * - parent schedule row decides recurrence + timing mode
 * - child fixed-time rows provide the actual clock times
 *
 * ## Time storage
 *
 * [time] is stored as a zero-padded 24-hour local time string:
 *
 * - "08:00"
 * - "12:30"
 * - "18:45"
 *
 * This is a time-of-day only value, not a timestamp.
 * The actual day is determined later by schedule evaluation.
 *
 * ## Why this is separate
 *
 * A meal may need:
 * - one fixed time per day
 * - multiple fixed times per day
 *
 * Keeping fixed times in child rows avoids packing time lists into one field
 * and matches the existing reusable scheduling UI architecture.
 *
 * ## Hard rules
 *
 * - Parent [MealScheduleEntity.timingType] should be FIXED_TIMES
 * - At least one row should exist for enabled fixed-time schedules
 * - Duplicate times for the same schedule should be avoided
 * - This table does NOT represent a concrete occurrence
 */
@Serializable
@Entity(
    tableName = "meal_schedule_fixed_times",
    foreignKeys = [
        ForeignKey(
            entity = MealScheduleEntity::class,
            parentColumns = ["id"],
            childColumns = ["scheduleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["scheduleId"]),
        Index(value = ["scheduleId", "time"], unique = true)
    ]
)
data class MealScheduleFixedTimeEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    /**
     * Parent meal schedule id.
     */
    val scheduleId: Long,

    /**
     * Zero-padded 24-hour local time string.
     * Example: "08:00"
     */
    val time: String
)