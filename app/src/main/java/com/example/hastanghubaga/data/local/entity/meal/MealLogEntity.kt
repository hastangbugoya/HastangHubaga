package com.example.hastanghubaga.data.local.entity.meal

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents one actual logged meal consumed by the user.
 *
 * Canonical meal model:
 * - [MealEntity] = reusable template / definition
 * - [MealOccurrenceEntity] = planned occurrence for a specific day/time
 * - [MealLogEntity] = actual consumed meal
 *
 * Reconciliation contract:
 * - if [occurrenceId] is non-null, this log fulfills that planned occurrence
 * - timeline builders may suppress the matching planned card and show only the log
 * - if [occurrenceId] is null, this is an extra / unplanned meal log
 *
 * Snapshot rule:
 * - [mealType] is copied into the log so history remains stable even if the
 *   template later changes
 *
 * Single-log-per-occurrence rule:
 * - at most one persisted log row may exist for a given non-null [occurrenceId]
 *
 * Minimal v1 scope:
 * - actual start/end timestamps
 * - optional notes
 * - optional nutrition snapshot
 */
@Entity(
    tableName = "meal_logs",
    foreignKeys = [
        ForeignKey(
            entity = MealEntity::class,
            parentColumns = ["id"],
            childColumns = ["mealId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("mealId"),
        Index(value = ["occurrenceId"], unique = true),
        Index("startTimestamp")
    ]
)
data class MealLogEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    /**
     * Optional template reference.
     */
    val mealId: Long? = null,

    /**
     * Optional planned occurrence fulfilled by this log.
     */
    val occurrenceId: String? = null,

    /**
     * Snapshot of meal type at log time.
     */
    val mealType: MealType,

    /**
     * Actual consumption start time.
     */
    val startTimestamp: Long,

    /**
     * Optional end time.
     */
    val endTimestamp: Long? = null,

    /**
     * Optional notes.
     */
    val notes: String? = null,

    // ---------------------------
    // Nutrition snapshot (optional)
    // ---------------------------

    val calories: Int? = null,
    val proteinGrams: Double? = null,
    val carbsGrams: Double? = null,
    val fatGrams: Double? = null,
    val sodiumMg: Double? = null,
    val cholesterolMg: Double? = null,
    val fiberGrams: Double? = null
)