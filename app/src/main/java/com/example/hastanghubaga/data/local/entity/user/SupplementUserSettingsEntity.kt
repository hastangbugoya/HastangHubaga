package com.example.hastanghubaga.data.local.entity.user

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.hastanghubaga.data.local.entity.supplement.SupplementDoseUnit
import kotlinx.serialization.Serializable

/**
 * Stores user-specific overrides for an existing supplement.
 *
 * This entity allows the user to customize how a supplement should behave
 * compared to the default values defined in [SupplementEntity].
 *
 * ## Purpose
 * Supplements come with recommended label values such as:
 * - serving size
 * - unit (capsule, mg, scoop, etc.)
 * - servings per day
 *
 * Users may wish to override these defaults based on:
 * - personal preference
 * - doctor's recommendation
 * - tolerance
 * - lifestyle patterns
 *
 * This table stores those overrides so the app can calculate personalized
 * daily intakes and scheduling logic.
 *
 * ## Behavioral Rules
 * - Any field set to `null` means *fallback to the supplement's default*.
 * - If `isEnabled` is false, the supplement should be hidden from daily
 *   reminders, Today screen, and calculations.
 *
 * ## Typical Uses
 * - Merge with [SupplementEntity] inside `SupplementWithSettings`
 * - Feed personalized doses into ingredient total calculations
 * - Control per-supplement activation/deactivation
 * - Store user notes (e.g., side effects, timing preferences)
 *
 * ## Database Notes
 * - This is not a 1:1 relation; each supplement may have at most one settings row.
 * - `supplementId` must reference an existing supplement.
 */
@Serializable
@Entity(tableName = "supplement_user_settings")
data class SupplementUserSettingsEntity(
    @PrimaryKey
    val supplementId: Long,

    /** User overrides recommended dosage */
    val preferredServingSize: Double? = null,
    val preferredUnit: SupplementDoseUnit? = null,

    val preferredServingPerDay: Double? = null,

    /** Whether the user wants this supplement active */
    val isEnabled: Boolean = true,

    /** Optional note the user adds */
    val userNotes: String? = null,

    /**
     * Determines how this supplement's schedule should be interpreted.
     *
     * This is the persistence-layer representation of the domain concept
     * `SupplementScheduleSpec` (e.g., FixedTimes vs MealAnchored).
     *
     * ### Rules
     * - If [scheduleType] is [ScheduleTypeEntity.FIXED_TIMES], then [fixedTimesCsv] should be non-null
     *   and [mealTypesCsv]/[mealOffsetMinutes] should be null.
     * - If [scheduleType] is [ScheduleTypeEntity.MEAL_ANCHORED], then [mealTypesCsv] may be non-null
     *   (one or more meal types) and [mealOffsetMinutes] may be non-null (defaults to 0),
     *   and [fixedTimesCsv] should be null.
     *
     * Defaulting to FIXED_TIMES keeps backwards compatibility for older rows/migrations.
     */
    val scheduleType: ScheduleTypeEntity = ScheduleTypeEntity.FIXED_TIMES,

    /**
     * CSV-encoded local times for fixed schedules.
     *
     * Format: "HH:mm,HH:mm,HH:mm" (24-hour time).
     * Example: "07:00,12:00,16:00"
     *
     * Only meaningful when [scheduleType] is [ScheduleTypeEntity.FIXED_TIMES].
     */
    val fixedTimesCsv: String? = null,

    /**
     * CSV-encoded meal types for meal-anchored schedules.
     *
     * Format: enum names joined by commas.
     * Example: "BREAKFAST,DINNER"
     *
     * Only meaningful when [scheduleType] is [ScheduleTypeEntity.MEAL_ANCHORED].
     */
    val mealTypesCsv: String? = null,

    /**
     * Offset in minutes applied to the resolved meal time.
     *
     * Example:
     * - 0   → exactly at the meal time
     * - 15  → 15 minutes after meal
     * - -30 → 30 minutes before meal
     *
     * Only meaningful when [scheduleType] is [ScheduleTypeEntity.MEAL_ANCHORED].
     */
    val mealOffsetMinutes: Int? = null
)
