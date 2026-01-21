package com.example.hastanghubaga.data.local.entity.supplement

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import java.time.DayOfWeek

/**
 * Represents a supplement defined by the user or preloaded into the system.
 *
 * This entity describes *what the supplement is*, *how it should be taken*, and
 * *its scheduling rules*. It does **not** describe nutritional ingredients
 * directly—those are modeled via `SupplementIngredientEntity`.
 *
 * ### Purpose
 * `SupplementEntity` is the core model powering:
 * - Daily schedule generation
 * - Reminder widgets + notifications
 * - Logging supplement intake
 * - Calculating daily nutrient totals
 * - Customizing dose units and serving sizes
 *
 * ### Scheduling Overview
 * Supplements can be taken:
 * - **Daily**
 * - **Every X days** (`frequencyInterval`)
 * - **Weekly** (`weeklyDays`)
 *
 * Each supplement also anchors to a day-relative event (e.g., wakeup, dinner)
 * via `doseAnchorType`, with optional `offsetMinutes`.
 *
 * ### Dose & Serving Overview
 * This class stores:
 * - Recommended serving size and unit
 * - Whether to take with food or liquids
 * - Whether caffeine interactions should be avoided
 * - Optional overrides for custom serving amounts
 *
 * ### Important Notes
 * - `startDate` is stored as ISO-8601 (`YYYY-MM-DD`)
 * - `lastTakenDate` is updated when the user logs an intake
 * - If a supplement becomes inactive (`isActive = false`), it is hidden from
 *   scheduling and reminders but preserved for history
 *
 * @property id Primary key.
 * @property name Display name of the supplement.
 * @property brand Optional manufacturer or product line.
 * @property notes Optional user notes about the supplement.
 *
 * @property recommendedServingSize Default serving size (e.g., 2 capsules).
 * @property recommendedDoseUnit Unit describing *how* the supplement is taken.
 * @property servingsPerDay How many times per day the serving is taken.
 * @property recommendedWithFood Whether food intake is recommended.
 * @property recommendedLiquidInOz Optional liquid amount recommended for swallowing.
 * @property recommendedTimeBetweenDailyDosesMinutes Minimum spacing between servings.
 * @property avoidCaffeine Whether caffeine should be avoided near this supplement.
 *
 * @property doseAnchorType The daily event used to anchor dose timing
 * (e.g., WAKEUP, DINNER, BEFORE_WORKOUT).
 * @property frequencyType Daily, weekly, or every X days.
 * @property frequencyInterval Number of days between doses (for EVERY_X_DAYS).
 * @property weeklyDays Days of the week to take it (for WEEKLY frequency).
 * @property offsetMinutes Minutes offset from the anchor time (can be negative).
 *
 * @property customDose User-defined override of serving size (optional).
 * @property customDoseUnit Unit for the custom dose (optional).
 *
 * @property startDate ISO date when the user started taking this supplement.
 * @property lastTakenDate ISO date when user last logged this supplement.
 *
 * @property isActive Whether the supplement is currently part of the user's regimen.
 *
 * @see SupplementDoseUnit
 * @see DoseAnchorType
 * @see FrequencyType
 * @see com.example.hastanghubaga.data.local.entity.supplement.SupplementIngredientEntity
 * @see com.example.hastanghubaga.data.local.entity.supplement.SupplementDailyLogEntity
 */
@Serializable
@Entity(tableName = "supplements")
data class SupplementEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val name: String,
    val brand: String?,
    val notes: String? = null,

    val recommendedServingSize: Double,    // e.g., 2 capsules
    val recommendedDoseUnit: SupplementDoseUnit,
    val servingsPerDay: Double,
    val recommendedWithFood: Boolean? = null,   // recommended to be taken with food
    val recommendedLiquidInOz: Double? = null,  // recommended with liquid in oz
    val recommendedTimeBetweenDailyDosesMinutes: Int? = null, // time between doses in minutes
    val avoidCaffeine: Boolean? = null, // should avoid taking with caffeine

    val doseAnchorType: DoseAnchorType = DoseAnchorType.MIDNIGHT,
    val frequencyType: FrequencyType = FrequencyType.DAILY,
    val frequencyInterval: Int? = null,              // used when EVERY_X_DAYS
    val weeklyDays: List<DayOfWeek>? = null,         // used when WEEKLY
    val offsetMinutes: Int? = null,

    val customDose: Double? = null,
    val customDoseUnit: SupplementDoseUnit? = null,
    val startDate: String? = null, // ISO-8601 "YYYY-MM-DD"
    val lastTakenDate: String? = null, // ISO-8601 "YYYY-MM-DD"

    val isActive: Boolean = true,
    @ColumnInfo(defaultValue = "0")
    val sendAlert: Boolean = false,
    val alertOffsetMinutes: Int? = null
)