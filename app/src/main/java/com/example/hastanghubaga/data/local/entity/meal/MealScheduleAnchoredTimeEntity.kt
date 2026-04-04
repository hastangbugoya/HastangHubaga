package com.example.hastanghubaga.data.local.entity.meal

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Anchored-time child rows for a meal schedule.
 *
 * This table stores one or more anchor-based timing rules for a parent
 * [MealScheduleEntity] whose timing mode is ANCHORED.
 *
 * This mirrors the activity scheduling pattern:
 *
 * - parent schedule row decides recurrence + timing mode
 * - anchored child rows define which anchor to use and what offset to apply
 *
 * ## Concept
 *
 * A meal may be scheduled relative to another event, such as:
 *
 * - BEFORE_BREAKFAST
 * - AFTER_BREAKFAST
 * - BEFORE_LUNCH
 * - AFTER_LUNCH
 * - BEFORE_DINNER
 * - AFTER_DINNER
 * - BEFORE_WORKOUT
 * - AFTER_WORKOUT
 *
 * The actual anchor-resolution pipeline happens later in the domain layer.
 * This table only persists the rule inputs.
 *
 * ## Storage choices
 *
 * [anchorType] is stored as a string for migration simplicity and schema
 * readability during this first meal-scheduling rollout.
 *
 * [offsetMinutes] stores the signed minute adjustment from the resolved anchor:
 *
 * - negative = before anchor
 * - positive = after anchor
 * - zero = exactly at anchor
 *
 * Examples:
 *
 * - anchorType = "BREAKFAST", offsetMinutes = -15
 * - anchorType = "DINNER", offsetMinutes = 30
 * - anchorType = "WORKOUT", offsetMinutes = 0
 *
 * ## Hard rules
 *
 * - Parent [MealScheduleEntity.timingType] should be ANCHORED
 * - At least one anchored row should exist for enabled anchored schedules
 * - Duplicate anchor+offset rows for the same schedule should be avoided
 * - This table does NOT represent a concrete occurrence
 */
@Serializable
@Entity(
    tableName = "meal_schedule_anchored_times",
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
        Index(value = ["scheduleId", "anchorType", "offsetMinutes"], unique = true)
    ]
)
data class MealScheduleAnchoredTimeEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    /**
     * Parent meal schedule id.
     */
    val scheduleId: Long,

    /**
     * Anchor type identifier.
     *
     * Suggested values should align with the reusable scheduling system's
     * anchor vocabulary so meals can share the same UI/editor flow as activities.
     */
    val anchorType: String,

    /**
     * Signed minute offset from the resolved anchor.
     *
     * Negative = before
     * Positive = after
     * Zero = exact anchor time
     */
    @ColumnInfo(defaultValue = "0")
    val offsetMinutes: Int = 0
)
