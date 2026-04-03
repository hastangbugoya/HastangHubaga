package com.example.hastanghubaga.data.local.entity.supplement

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import java.time.DayOfWeek

/**
 * Represents a supplement defined by the user or preloaded into the system.
 *
 * -------------------------------------------------------------------------
 * CURRENT ARCHITECTURE NOTE (IMPORTANT)
 * -------------------------------------------------------------------------
 * This entity currently contains **two generations of scheduling data**:
 *
 * 1) ⚠️ LEGACY (DEPRECATED FOR SCHEDULING)
 *    - doseAnchorType
 *    - frequencyType
 *    - frequencyInterval
 *    - weeklyDays
 *    - offsetMinutes
 *    - startDate
 *
 *    These fields were part of the original "inline scheduling model".
 *    They are **NO LONGER USED by the scheduling engine**.
 *
 * 2) ✅ CURRENT SYSTEM (AUTHORITATIVE)
 *    Scheduling is now fully driven by:
 *    - SupplementScheduleEntity
 *    - SupplementScheduleFixedTimeEntity
 *    - SupplementScheduleAnchoredTimeEntity
 *
 *    These define:
 *    - recurrence rules
 *    - timing (fixed or anchored)
 *    - enable/disable state
 *
 * -------------------------------------------------------------------------
 * RULE OF TRUTH
 * -------------------------------------------------------------------------
 * The deterministic planner ONLY reads from:
 *
 * 👉 supplement_schedules + child tables
 *
 * It DOES NOT read any scheduling fields from this entity.
 *
 * -------------------------------------------------------------------------
 * WHY LEGACY FIELDS STILL EXIST
 * -------------------------------------------------------------------------
 * - Backward compatibility with existing DB
 * - Avoid destructive migrations during active development
 * - May be used for:
 *   - display hints
 *   - future migration/backfill
 *
 * -------------------------------------------------------------------------
 * IMPORTANT FOR DEVELOPERS
 * -------------------------------------------------------------------------
 * ❌ DO NOT use legacy fields for scheduling logic
 * ❌ DO NOT read these fields in planner or timeline code
 *
 * ✅ ONLY use schedule tables for scheduling decisions
 *
 * -------------------------------------------------------------------------
 * PURPOSE
 * -------------------------------------------------------------------------
 * This entity now primarily represents:
 *
 * - supplement identity (name, brand, notes)
 * - dosing characteristics
 * - user preferences
 *
 * NOT scheduling logic.
 *
 * -------------------------------------------------------------------------
 * @property id Primary key.
 * @property name Display name of the supplement.
 * @property brand Optional manufacturer or product line.
 * @property notes Optional user notes.
 *
 * @property recommendedServingSize Default serving size.
 * @property recommendedDoseUnit Unit describing intake form.
 * @property servingsPerDay Suggested number of servings.
 * @property recommendedWithFood Whether food is recommended.
 * @property recommendedLiquidInOz Optional liquid recommendation.
 * @property recommendedTimeBetweenDailyDosesMinutes Suggested spacing.
 * @property avoidCaffeine Whether caffeine should be avoided.
 *
 * -------------------------------------------------------------------------
 * ⚠️ LEGACY SCHEDULING FIELDS (DO NOT USE FOR NEW LOGIC)
 * -------------------------------------------------------------------------
 * @property doseAnchorType Legacy anchor reference
 * @property frequencyType Legacy recurrence type
 * @property frequencyInterval Legacy interval (EVERY_X_DAYS)
 * @property weeklyDays Legacy weekly schedule
 * @property offsetMinutes Legacy offset from anchor
 * @property startDate Legacy schedule start date
 *
 * -------------------------------------------------------------------------
 * @property customDose Optional override for serving size.
 * @property customDoseUnit Unit for custom dose.
 *
 * @property lastTakenDate Last logged intake (still valid).
 *
 * @property isActive Whether supplement is part of regimen.
 *
 * @property sendAlert Whether alerts are enabled.
 * @property alertOffsetMinutes Alert offset.
 *
 * @see SupplementScheduleEntity (NEW scheduling system)
 */
@Serializable
@Entity(tableName = "supplements")
data class SupplementEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val name: String,
    val brand: String?,
    val notes: String? = null,

    val recommendedServingSize: Double,
    val recommendedDoseUnit: SupplementDoseUnit,
    val servingsPerDay: Double,
    val recommendedWithFood: Boolean? = null,
    val recommendedLiquidInOz: Double? = null,
    val recommendedTimeBetweenDailyDosesMinutes: Int? = null,
    val avoidCaffeine: Boolean? = null,

    // ---------------------------------------------------------------------
    // ⚠️ LEGACY SCHEDULING FIELDS (NO LONGER USED BY PLANNER)
    // ---------------------------------------------------------------------
    val doseAnchorType: DoseAnchorType = DoseAnchorType.MIDNIGHT,
    val frequencyType: FrequencyType = FrequencyType.DAILY,
    val frequencyInterval: Int? = null,
    val weeklyDays: List<DayOfWeek>? = null,
    val offsetMinutes: Int? = null,

    val customDose: Double? = null,
    val customDoseUnit: SupplementDoseUnit? = null,
    val startDate: String? = null, // legacy scheduling start date
    val lastTakenDate: String? = null,

    val isActive: Boolean = true,

    @ColumnInfo(defaultValue = "0")
    val sendAlert: Boolean = false,

    val alertOffsetMinutes: Int? = null
)