package com.example.hastanghubaga.data.local.entity.meal

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Represents a reusable meal template.
 *
 * This is NOT a point-in-time event.
 * This defines WHAT a meal is — not WHEN it happens.
 *
 * Time is determined later by:
 * - Meal scheduling (fixed times or anchored times)
 * - Future meal occurrence materialization
 *
 * This aligns meals with the same architecture as activities:
 *
 * template -> schedule -> occurrence -> (future) log -> timeline merge
 *
 * ## Key Rules
 *
 * - This entity MUST NOT store timestamps
 * - This entity MUST NOT represent a specific day occurrence
 * - This entity is reusable across days
 *
 * ## Field rationale
 *
 * - [id]
 *   Local DB identity.
 *
 * - [name]
 *   User-defined label (e.g., "Breakfast", "Post Workout Shake")
 *
 * - [type]
 *   Semantic category (BREAKFAST, LUNCH, DINNER, SNACK, etc.)
 *
 * - [treatAsAnchor]
 *   Optional override for scheduling anchors.
 *   Does NOT change the actual type.
 *
 * - [isActive]
 *   Whether this meal participates in scheduling.
 *   Inactive meals do not generate planned timeline rows.
 */
@Serializable
@Entity(tableName = "meals")
data class MealEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    @ColumnInfo(defaultValue = "")
    val name: String = "",

    val type: MealType,

    val treatAsAnchor: MealType? = null,

    @ColumnInfo(defaultValue = "1")
    val isActive: Boolean = true
)