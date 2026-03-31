package com.example.hastanghubaga.domain.model.supplement

import com.example.hastanghubaga.data.local.entity.supplement.SupplementDoseUnit
import com.example.hastanghubaga.domain.schedule.model.TimeAnchor
import kotlinx.datetime.LocalTime

/**
 * Concrete same-day resolved supplement timing output.
 *
 * WHY THIS EXISTS
 *
 * Persisted supplement schedules are richer than a flat [List] of [LocalTime]:
 * - one supplement may have multiple parent schedule rows
 * - one schedule row may have multiple fixed or anchored child timing rows
 * - anchored child rows carry their own offset/label/order
 *
 * This model preserves enough identity to let the scheduling pipeline:
 * - resolve a supplement into one or more concrete same-day planned times
 * - keep fixed and anchored results in one normalized shape
 * - preserve schedule-level identity for future occurrence-aware logging
 * - support future "strict every N days" behavior without redesigning the domain model
 *
 * COMPATIBILITY NOTES
 *
 * - [scheduleId] is nullable so legacy / user-settings-derived timings can still be represented
 * - [sourceRowId] is nullable for the same reason
 * - [anchor] is only populated for anchored timings
 */
data class ResolvedSupplementScheduleEntry(
    val scheduleId: Long? = null,
    val sourceRowId: Long? = null,
    val time: LocalTime,
    val timingType: ResolvedSupplementTimingType,
    val anchor: TimeAnchor? = null,
    val label: String? = null,
    val sortOrder: Int = 0
)

/**
 * Normalized timing source for a resolved supplement schedule entry.
 */
enum class ResolvedSupplementTimingType {
    FIXED,
    ANCHORED,
    LEGACY
}

/**
 * Domain model representing a supplement combined with
 * user-specific preferences and overrides.
 *
 * This is the model most screens should use when displaying:
 * - recommended vs preferred dosage
 * - enabled/disabled state
 * - user notes
 *
 * It intentionally wraps the base [Supplement] instead of
 * modifying it, keeping domain responsibilities clean.
 *
 * WHY SupplementWithUserSettings EXISTS
 *
 * - UI needs "recommended vs preferred" in one place
 * - User settings may be missing
 * - Domain must not depend on Room @Relation models
 * - ViewModels should not combine flows manually
 *
 * RULES:
 * - Never store user settings inside Supplement
 * - Never expose Room join models to UI
 * - Always return this model for screens
 *
 * TESTING NOTES:
 * - Flow emits multiple times on REPLACE
 * - Initial emission may contain null userSettings
 * - Use take(n) in tests
 */
data class SupplementWithUserSettings(
    val supplement: Supplement,
    val userSettings: UserSupplementSettings?,
    val doseState: MealAwareDoseState,

    /**
     * Backward-compatible flat time list used by older callers.
     *
     * New scheduling code should prefer [resolvedScheduleEntries] because
     * [scheduledTimes] cannot preserve which schedule row produced each time.
     */
    val scheduledTimes: List<LocalTime> = emptyList(),

    /**
     * Transitional high-level schedule intent.
     *
     * This remains useful for legacy / compatibility paths, but it is not
     * rich enough to faithfully represent all persisted schedule rows when a
     * supplement has multiple schedules or multiple anchored child rows with
     * different offsets.
     */
    val scheduleSpec: SupplementScheduleSpec? = null,

    /**
     * Concrete resolved same-day schedule outputs for this supplement.
     *
     * This is the preferred daily scheduling output for timeline, reminders,
     * and future occurrence-aware supplement flows.
     *
     * It intentionally coexists with [scheduledTimes] during migration so
     * existing callers do not break while newer callers move to this richer
     * shape.
     */
    val resolvedScheduleEntries: List<ResolvedSupplementScheduleEntry> = emptyList()
) {

    /** Preferred dose if user overrides it, otherwise recommended */
    val effectiveServingSize: Double
        get() = userSettings?.preferredServingSize
            ?: supplement.recommendedServingSize

    /** Preferred unit if overridden */
    val effectiveDoseUnit: SupplementDoseUnit
        get() = userSettings?.preferredUnit
            ?: supplement.recommendedDoseUnit

    /** Preferred servings per day */
    val effectiveServingsPerDay: Double
        get() = userSettings?.preferredServingsPerDay
            ?: supplement.servingsPerDay

    /** Whether this supplement is enabled for the user */
    val isEnabled: Boolean
        get() = userSettings?.isEnabled
            ?: supplement.isActive

    /** Optional user notes */
    val userNotes: String?
        get() = userSettings?.notes
}