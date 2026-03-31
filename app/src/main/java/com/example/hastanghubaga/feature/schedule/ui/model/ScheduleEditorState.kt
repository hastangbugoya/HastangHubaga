package com.example.hastanghubaga.feature.schedule.ui.model

import kotlinx.datetime.LocalDate

/**
 * Top-level UI state for the shared Schedule Editor.
 *
 * This is intentionally domain-neutral and Compose-friendly.
 * It should NOT expose domain models directly.
 */
data class ScheduleEditorState(

    // -------------------------
    // Enable / disable
    // -------------------------
    val isEnabled: Boolean = false,

    // -------------------------
    // Recurrence
    // -------------------------
    val recurrenceMode: RecurrenceMode = RecurrenceMode.DAILY,

    /**
     * Interval as raw user input (string for TextField friendliness)
     * Example:
     * - "1" = every day
     * - "2" = every 2 days
     */
    val intervalInput: String = "1",

    /**
     * Used only for WEEKLY mode
     */
    val selectedWeekdays: Set<WeekdayUi> = emptySet(),

    // -------------------------
    // Date range
    // -------------------------
    val startDate: LocalDate? = null,

    val hasEndDate: Boolean = false,
    val endDate: LocalDate? = null,

    // -------------------------
    // Timing
    // -------------------------
    val timingMode: TimingMode = TimingMode.FIXED,

    val fixedTimes: List<FixedTimeRowUi> = emptyList(),

    val anchoredTimes: List<AnchoredTimeRowUi> = emptyList(),

    // -------------------------
    // Validation + summary
    // -------------------------
    val validationErrors: List<ScheduleValidationError> = emptyList(),

    val summaryText: String = ""
)

/**
 * Recurrence mode selector
 */
enum class RecurrenceMode {
    DAILY,
    WEEKLY
}

/**
 * Timing mode selector
 */
enum class TimingMode {
    FIXED,
    ANCHORED
}

/**
 * Simple weekday representation for UI selection
 */
enum class WeekdayUi {
    MONDAY,
    TUESDAY,
    WEDNESDAY,
    THURSDAY,
    FRIDAY,
    SATURDAY,
    SUNDAY
}

/**
 * Fixed time row (e.g., 08:00, 13:30)
 *
 * Keep as raw string for now to avoid premature parsing complexity.
 */
data class FixedTimeRowUi(
    val id: String,
    val timeInput: String = "" // "08:00"
)

/**
 * Anchored time row (e.g., BEFORE_MEAL - 30 min)
 */
data class AnchoredTimeRowUi(
    val id: String,
    val anchor: AnchorTypeUi = AnchorTypeUi.WAKE_UP,
    val offsetMinutesInput: String = "0"
)

/**
 * UI-level anchor types
 * (Mapped later to domain TimeAnchor)
 */
enum class AnchorTypeUi {
    WAKE_UP,
    BREAKFAST,
    LUNCH,
    DINNER,
    SLEEP,

    BEFORE_WORKOUT,
    DURING_WORKOUT,
    AFTER_WORKOUT
}

/**
 * Validation errors for UI display
 */
sealed class ScheduleValidationError {

    object MissingStartDate : ScheduleValidationError()

    object InvalidInterval : ScheduleValidationError()

    object NoWeekdaysSelected : ScheduleValidationError()

    object NoTimesDefined : ScheduleValidationError()

    data class InvalidTimeFormat(val rowId: String) : ScheduleValidationError()

    data class InvalidOffset(val rowId: String) : ScheduleValidationError()

    object EndDateBeforeStartDate : ScheduleValidationError()
}