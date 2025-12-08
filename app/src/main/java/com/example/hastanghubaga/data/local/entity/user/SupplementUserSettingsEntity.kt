package com.example.hastanghubaga.data.local.entity.user

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.hastanghubaga.data.local.entity.supplement.SupplementDoseUnit

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
@Entity(tableName = "supplement_user_settings")
data class SupplementUserSettingsEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val supplementId: Long,

    /** User overrides recommended dosage */
    val preferredServingSize: Double? = null,
    val preferredUnit: SupplementDoseUnit? = null,

    val preferredServingPerDay: Int? = null,


    /** Whether the user wants this supplement active */
    val isEnabled: Boolean = true,

    /** Optional note the user adds */
    val userNotes: String? = null
)
