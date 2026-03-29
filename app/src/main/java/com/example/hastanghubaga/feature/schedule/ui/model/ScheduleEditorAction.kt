package com.example.hastanghubaga.feature.schedule.ui.model

import kotlinx.datetime.LocalDate

/**
 * Explicit user actions for the shared schedule editor UI.
 *
 * This keeps the editor reusable and makes ViewModel integration much cleaner
 * than passing a growing number of ad hoc callbacks around.
 */
sealed interface ScheduleEditorAction {

    // -------------------------
    // Enable / disable
    // -------------------------
    data class SetEnabled(val isEnabled: Boolean) : ScheduleEditorAction

    // -------------------------
    // Recurrence
    // -------------------------
    data class SetRecurrenceMode(
        val recurrenceMode: RecurrenceMode
    ) : ScheduleEditorAction

    data class SetIntervalInput(
        val value: String
    ) : ScheduleEditorAction

    data class ToggleWeekday(
        val weekday: WeekdayUi
    ) : ScheduleEditorAction

    // -------------------------
    // Date range
    // -------------------------
    data class SetStartDate(
        val date: LocalDate
    ) : ScheduleEditorAction

    data class SetHasEndDate(
        val hasEndDate: Boolean
    ) : ScheduleEditorAction

    data class SetEndDate(
        val date: LocalDate?
    ) : ScheduleEditorAction

    // -------------------------
    // Timing mode
    // -------------------------
    data class SetTimingMode(
        val timingMode: TimingMode
    ) : ScheduleEditorAction

    // -------------------------
    // Fixed times
    // -------------------------
    data object AddFixedTimeRow : ScheduleEditorAction

    data class RemoveFixedTimeRow(
        val rowId: String
    ) : ScheduleEditorAction

    data class SetFixedTimeValue(
        val rowId: String,
        val value: String
    ) : ScheduleEditorAction

    // -------------------------
    // Anchored times
    // -------------------------
    data object AddAnchoredTimeRow : ScheduleEditorAction

    data class RemoveAnchoredTimeRow(
        val rowId: String
    ) : ScheduleEditorAction

    data class SetAnchoredRowAnchor(
        val rowId: String,
        val anchor: AnchorTypeUi
    ) : ScheduleEditorAction

    data class SetAnchoredRowOffsetValue(
        val rowId: String,
        val value: String
    ) : ScheduleEditorAction

    // -------------------------
    // Utility / derived-state triggers
    // -------------------------
    data object Validate : ScheduleEditorAction

    data object RefreshSummary : ScheduleEditorAction
}