package com.example.hastanghubaga.domain.model.supplement

import com.example.hastanghubaga.data.local.entity.supplement.SupplementDoseUnit
import kotlinx.datetime.LocalTime


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

 WHY SupplementWithUserSettings EXISTS

 - UI needs "recommended vs preferred" in one place
 - User settings may be missing
 - Domain must not depend on Room @Relation models
 - ViewModels should not combine flows manually

 RULES:
 - Never store user settings inside Supplement
 - Never expose Room join models to UI
 - Always return this model for screens

 TESTING NOTES:
 - Flow emits multiple times on REPLACE
 - Initial emission may contain null userSettings
 - Use take(n) in tests
 */
data class SupplementWithUserSettings(
    val supplement: Supplement,
    val userSettings: UserSupplementSettings?,
    val doseState: MealAwareDoseState,
    val scheduledTimes: List<LocalTime> = emptyList(),
    /**
     * The user's schedule rule for when this supplement should occur.
     *
     * This is the "source of truth" schedule intent (e.g. fixed times, or anchored to meals).
     * It is intentionally a domain model (not Room) so timeline building, alerts, and widgets
     * can resolve it into concrete occurrences for a given date.
     *
     * Note: [scheduledTimes] is still present for backward compatibility while the app is
     * transitioning to schedule resolution via [SupplementScheduleSpec]. Over time, callers
     * should prefer [scheduleSpec] and derive daily occurrences via a resolver use case.
     */
    val scheduleSpec: SupplementScheduleSpec? = null
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
