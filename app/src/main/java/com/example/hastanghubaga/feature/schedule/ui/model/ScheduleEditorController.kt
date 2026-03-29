package com.example.hastanghubaga.feature.schedule.ui.model

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlinx.datetime.LocalDate

/**
 * Small shared controller/state-holder for the schedule editor.
 *
 * This keeps host screens lighter by centralizing:
 * - current editor state
 * - action dispatch
 * - common convenience methods
 *
 * This is intentionally UI-layer only.
 * It does not do persistence or feature-specific orchestration.
 */
@Stable
class ScheduleEditorController(
    initialState: ScheduleEditorState = ScheduleEditorReducer.initialState()
) {

    var state: ScheduleEditorState = initialState
        private set

    fun dispatch(action: ScheduleEditorAction) {
        state = ScheduleEditorReducer.reduce(state, action)
    }

    fun setEnabled(isEnabled: Boolean) {
        dispatch(ScheduleEditorAction.SetEnabled(isEnabled))
    }

    fun setRecurrenceMode(mode: RecurrenceMode) {
        dispatch(ScheduleEditorAction.SetRecurrenceMode(mode))
    }

    fun setIntervalInput(value: String) {
        dispatch(ScheduleEditorAction.SetIntervalInput(value))
    }

    fun toggleWeekday(weekday: WeekdayUi) {
        dispatch(ScheduleEditorAction.ToggleWeekday(weekday))
    }

    fun setStartDate(date: LocalDate) {
        dispatch(ScheduleEditorAction.SetStartDate(date))
    }

    fun setHasEndDate(hasEndDate: Boolean) {
        dispatch(ScheduleEditorAction.SetHasEndDate(hasEndDate))
    }

    fun setEndDate(date: LocalDate?) {
        dispatch(ScheduleEditorAction.SetEndDate(date))
    }

    fun setTimingMode(mode: TimingMode) {
        dispatch(ScheduleEditorAction.SetTimingMode(mode))
    }

    fun addFixedTimeRow() {
        dispatch(ScheduleEditorAction.AddFixedTimeRow)
    }

    fun removeFixedTimeRow(rowId: String) {
        dispatch(ScheduleEditorAction.RemoveFixedTimeRow(rowId))
    }

    fun setFixedTimeValue(rowId: String, value: String) {
        dispatch(ScheduleEditorAction.SetFixedTimeValue(rowId, value))
    }

    fun addAnchoredTimeRow() {
        dispatch(ScheduleEditorAction.AddAnchoredTimeRow)
    }

    fun removeAnchoredTimeRow(rowId: String) {
        dispatch(ScheduleEditorAction.RemoveAnchoredTimeRow(rowId))
    }

    fun setAnchoredRowAnchor(rowId: String, anchor: AnchorTypeUi) {
        dispatch(ScheduleEditorAction.SetAnchoredRowAnchor(rowId, anchor))
    }

    fun setAnchoredRowOffsetValue(rowId: String, value: String) {
        dispatch(ScheduleEditorAction.SetAnchoredRowOffsetValue(rowId, value))
    }

    fun validate() {
        dispatch(ScheduleEditorAction.Validate)
    }

    /**
     * Host screens can call this later once summary computation is wired to the
     * actual scheduling domain summary bridge.
     */
    fun refreshSummary() {
        dispatch(ScheduleEditorAction.RefreshSummary)
    }

    /**
     * Replace the whole editor state, useful when a host screen later loads an
     * existing schedule from persistence and maps it into UI state.
     */
    fun replaceState(newState: ScheduleEditorState) {
        state = ScheduleEditorMapper.validate(newState)
    }

    /**
     * Returns the parsed editor draft if the current state is valid.
     * Useful for later save/apply flows.
     */
    fun parsedOrNull(): ParsedScheduleEditorDraft? {
        return when (val result = ScheduleEditorMapper.parse(state)) {
            is ParseResult.Valid -> result.draft
            is ParseResult.Invalid -> null
        }
    }

    /**
     * Convenience validity check for host screens.
     */
    fun isValid(): Boolean {
        return state.validationErrors.isEmpty()
    }

    fun snapshot(): ScheduleEditorSnapshot {
        return ScheduleEditorSnapshot(
            state = state,
            parsedDraft = parsedOrNull()
        )
    }
}

/**
 * Lightweight immutable snapshot for hosts that want both the raw state and
 * the parsed draft at the same time.
 */
@Immutable
data class ScheduleEditorSnapshot(
    val state: ScheduleEditorState,
    val parsedDraft: ParsedScheduleEditorDraft?
)