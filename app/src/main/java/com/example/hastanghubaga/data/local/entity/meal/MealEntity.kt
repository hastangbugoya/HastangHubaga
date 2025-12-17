package com.example.hastanghubaga.data.local.entity.meal

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Represents a concrete meal occurrence recorded by the system.
 *
 * A meal is modeled as a point-in-time event (not a schedule rule), meaning it
 * always has a fully resolved timestamp rather than an anchor or time-of-day.
 *
 * ## Time semantics (IMPORTANT)
 * - [timestamp] is stored as **UTC epoch milliseconds**
 * - UTC is used to ensure:
 *   - Stable ordering across days
 *   - Correct behavior across time zones and DST changes
 *   - Safe comparison with supplements, activities, and logs
 *
 * Conversion to local time MUST happen at the UI or presentation layer:
 *
 * ```
 * Instant.ofEpochMilli(timestamp)
 *     .atZone(ZoneId.systemDefault())
 * ```
 *
 * The database MUST NOT store local time values.
 *
 * ## Field rationale
 * - [id]
 *   Auto-generated primary key. Used only for local identity and relations.
 *
 * - [type]
 *   The semantic meal category (e.g., BREAKFAST, LUNCH, DINNER).
 *   Used for grouping, display, and future timeline ordering.
 *
 * - [timestamp]
 *   The moment the meal occurred, stored as UTC epoch milliseconds.
 *   This is the authoritative time reference for all meal-related logic.
 *
 * - [notes]
 *   Optional user-provided text. Does not affect scheduling or timeline logic.
 */
@Serializable
@Entity(tableName = "meals")
data class MealEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val type: MealType,
    val timestamp: Long, // epoch millis

    val notes: String? = null
)
