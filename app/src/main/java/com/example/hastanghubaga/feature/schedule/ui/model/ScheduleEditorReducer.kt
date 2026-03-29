package com.example.hastanghubaga.feature.schedule.ui.model


/**
 * Pure reducer for shared schedule editor UI state.
 *
 * Responsibilities:
 * - apply explicit UI actions
 * - keep add/remove/toggle logic centralized
 * - normalize mode transitions
 * - re-run UI-level validation
 *
 * Non-responsibilities for now:
 * - persistence
 * - domain ScheduleRule construction
 * - feature-specific rules
 * - date picker orchestration
 */
object ScheduleEditorReducer {

    fun reduce(
        state: ScheduleEditorState,
        action: ScheduleEditorAction
    ): ScheduleEditorState {
        val updated = when (action) {
            is ScheduleEditorAction.SetEnabled -> {
                state.copy(isEnabled = action.isEnabled)
            }

            is ScheduleEditorAction.SetRecurrenceMode -> {
                ScheduleEditorMapper
                    .normalizeForRecurrenceModeChange(
                        state.copy(recurrenceMode = action.recurrenceMode)
                    )
            }

            is ScheduleEditorAction.SetIntervalInput -> {
                state.copy(intervalInput = action.value)
            }

            is ScheduleEditorAction.ToggleWeekday -> {
                state.copy(
                    selectedWeekdays = state.selectedWeekdays.toggle(action.weekday)
                )
            }

            is ScheduleEditorAction.SetStartDate -> {
                state.copy(startDate = action.date)
            }

            is ScheduleEditorAction.SetHasEndDate -> {
                if (action.hasEndDate) {
                    state.copy(hasEndDate = true)
                } else {
                    state.copy(
                        hasEndDate = false,
                        endDate = null
                    )
                }
            }

            is ScheduleEditorAction.SetEndDate -> {
                state.copy(endDate = action.date)
            }

            is ScheduleEditorAction.SetTimingMode -> {
                ScheduleEditorMapper
                    .normalizeForModeChange(
                        state.copy(timingMode = action.timingMode)
                    )
            }

            ScheduleEditorAction.AddFixedTimeRow -> {
                state.copy(
                    fixedTimes = state.fixedTimes + ScheduleEditorMapper.newFixedTimeRow()
                )
            }

            is ScheduleEditorAction.RemoveFixedTimeRow -> {
                val updatedRows = state.fixedTimes.filterNot { it.id == action.rowId }
                state.copy(
                    fixedTimes = if (updatedRows.isEmpty()) {
                        listOf(ScheduleEditorMapper.newFixedTimeRow())
                    } else {
                        updatedRows
                    }
                )
            }

            is ScheduleEditorAction.SetFixedTimeValue -> {
                state.copy(
                    fixedTimes = state.fixedTimes.map { row ->
                        if (row.id == action.rowId) {
                            row.copy(timeInput = action.value)
                        } else {
                            row
                        }
                    }
                )
            }

            ScheduleEditorAction.AddAnchoredTimeRow -> {
                state.copy(
                    anchoredTimes = state.anchoredTimes + ScheduleEditorMapper.newAnchoredTimeRow()
                )
            }

            is ScheduleEditorAction.RemoveAnchoredTimeRow -> {
                val updatedRows = state.anchoredTimes.filterNot { it.id == action.rowId }
                state.copy(
                    anchoredTimes = if (updatedRows.isEmpty()) {
                        listOf(ScheduleEditorMapper.newAnchoredTimeRow())
                    } else {
                        updatedRows
                    }
                )
            }

            is ScheduleEditorAction.SetAnchoredRowAnchor -> {
                state.copy(
                    anchoredTimes = state.anchoredTimes.map { row ->
                        if (row.id == action.rowId) {
                            row.copy(anchor = action.anchor)
                        } else {
                            row
                        }
                    }
                )
            }

            is ScheduleEditorAction.SetAnchoredRowOffsetValue -> {
                state.copy(
                    anchoredTimes = state.anchoredTimes.map { row ->
                        if (row.id == action.rowId) {
                            row.copy(offsetMinutesInput = action.value)
                        } else {
                            row
                        }
                    }
                )
            }

            ScheduleEditorAction.Validate -> {
                state
            }

            ScheduleEditorAction.RefreshSummary -> {
                state
            }
        }

        return when (action) {
            ScheduleEditorAction.RefreshSummary -> updated
            else -> ScheduleEditorMapper.validate(updated)
        }
    }

    /**
     * Safe initial/default editor state for new integrations.
     *
     * Gives the active timing mode one editable row immediately so the editor
     * is usable without extra setup in the host screen/ViewModel.
     */
    fun initialState(): ScheduleEditorState {
        val base = ScheduleEditorState(
            isEnabled = false,
            recurrenceMode = RecurrenceMode.DAILY,
            intervalInput = "1",
            timingMode = TimingMode.FIXED,
            fixedTimes = listOf(ScheduleEditorMapper.newFixedTimeRow())
        )

        return ScheduleEditorMapper.validate(base)
    }
}

private fun Set<WeekdayUi>.toggle(weekday: WeekdayUi): Set<WeekdayUi> {
    return if (weekday in this) {
        this - weekday
    } else {
        this + weekday
    }
}