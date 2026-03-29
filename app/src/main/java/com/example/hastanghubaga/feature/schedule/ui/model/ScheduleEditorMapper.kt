package com.example.hastanghubaga.feature.schedule.ui.model

import kotlinx.datetime.LocalDate

/**
 * Pure mapper/parser helpers for the shared schedule editor UI.
 *
 * This file intentionally stops one step short of constructing the domain
 * ScheduleRule directly, because the exact domain constructor/factory shape
 * should match the existing scheduling engine already in the project.
 *
 * That keeps this layer compile-safe, JUnit-testable, and free from guessing
 * incorrect domain signatures.
 */
object ScheduleEditorMapper {

    /**
     * Parses raw UI state into a validated typed draft that is ready for:
     * - domain mapping
     * - summary formatting
     * - validation bridge use cases
     *
     * Returns a failure with UI-friendly validation errors if any required
     * fields are missing or malformed.
     */
    fun parse(state: ScheduleEditorState): ParseResult {
        val errors = mutableListOf<ScheduleValidationError>()

        val interval = state.intervalInput.toIntOrNull()
            ?.takeIf { it > 0 }
            ?: run {
                errors += ScheduleValidationError.InvalidInterval
                null
            }

        val startDate = state.startDate ?: run {
            errors += ScheduleValidationError.MissingStartDate
            null
        }

        val weeklyDays = when (state.recurrenceMode) {
            RecurrenceMode.DAILY -> emptySet()
            RecurrenceMode.WEEKLY -> {
                if (state.selectedWeekdays.isEmpty()) {
                    errors += ScheduleValidationError.NoWeekdaysSelected
                }
                state.selectedWeekdays
            }
        }

        val fixedTimes = when (state.timingMode) {
            TimingMode.FIXED -> {
                if (state.fixedTimes.isEmpty()) {
                    errors += ScheduleValidationError.NoTimesDefined
                }

                state.fixedTimes.mapNotNull { row ->
                    parseTimeInput(row.timeInput)?.let { parsed ->
                        ParsedFixedTimeRow(
                            id = row.id,
                            hour = parsed.hour,
                            minute = parsed.minute
                        )
                    } ?: run {
                        errors += ScheduleValidationError.InvalidTimeFormat(row.id)
                        null
                    }
                }
            }

            TimingMode.ANCHORED -> emptyList()
        }

        val anchoredTimes = when (state.timingMode) {
            TimingMode.FIXED -> emptyList()

            TimingMode.ANCHORED -> {
                if (state.anchoredTimes.isEmpty()) {
                    errors += ScheduleValidationError.NoTimesDefined
                }

                state.anchoredTimes.mapNotNull { row ->
                    val offsetMinutes = row.offsetMinutesInput.toIntOrNull()
                    if (offsetMinutes == null) {
                        errors += ScheduleValidationError.InvalidOffset(row.id)
                        null
                    } else {
                        ParsedAnchoredTimeRow(
                            id = row.id,
                            anchor = row.anchor,
                            offsetMinutes = offsetMinutes
                        )
                    }
                }
            }
        }

        if (startDate != null && state.hasEndDate && state.endDate != null) {
            if (state.endDate < startDate) {
                errors += ScheduleValidationError.EndDateBeforeStartDate
            }
        }

        if (errors.isNotEmpty() || interval == null || startDate == null) {
            return ParseResult.Invalid(errors.distinct())
        }

        return ParseResult.Valid(
            ParsedScheduleEditorDraft(
                isEnabled = state.isEnabled,
                recurrenceMode = state.recurrenceMode,
                interval = interval,
                selectedWeekdays = weeklyDays,
                startDate = startDate,
                hasEndDate = state.hasEndDate,
                endDate = state.endDate,
                timingMode = state.timingMode,
                fixedTimes = fixedTimes,
                anchoredTimes = anchoredTimes
            )
        )
    }

    /**
     * Produces a normalized state with validation errors updated.
     *
     * Useful for view models:
     * 1. user edits raw fields
     * 2. VM calls validate(...)
     * 3. UI re-renders with row/global errors
     */
    fun validate(state: ScheduleEditorState): ScheduleEditorState {
        return when (val result = parse(state)) {
            is ParseResult.Valid -> state.copy(validationErrors = emptyList())
            is ParseResult.Invalid -> state.copy(validationErrors = result.errors)
        }
    }

    /**
     * Clears timing rows that do not apply to the selected timing mode and
     * ensures there is at least one editable row for the active mode.
     *
     * This is intentionally UI-only normalization logic.
     */
    fun normalizeForModeChange(state: ScheduleEditorState): ScheduleEditorState {
        return when (state.timingMode) {
            TimingMode.FIXED -> state.copy(
                fixedTimes = if (state.fixedTimes.isEmpty()) {
                    listOf(newFixedTimeRow())
                } else {
                    state.fixedTimes
                },
                anchoredTimes = emptyList()
            )

            TimingMode.ANCHORED -> state.copy(
                fixedTimes = emptyList(),
                anchoredTimes = if (state.anchoredTimes.isEmpty()) {
                    listOf(newAnchoredTimeRow())
                } else {
                    state.anchoredTimes
                }
            )
        }
    }

    /**
     * Ensures weekly mode has a sensible weekday seed if a caller wants it.
     * Kept separate so the VM can choose whether to auto-seed or not.
     */
    fun normalizeForRecurrenceModeChange(state: ScheduleEditorState): ScheduleEditorState {
        return when (state.recurrenceMode) {
            RecurrenceMode.DAILY -> state.copy(selectedWeekdays = emptySet())
            RecurrenceMode.WEEKLY -> state.copy(
                selectedWeekdays = if (state.selectedWeekdays.isEmpty()) {
                    setOf(WeekdayUi.MONDAY)
                } else {
                    state.selectedWeekdays
                }
            )
        }
    }

    fun newFixedTimeRow(id: String = generateRowId()): FixedTimeRowUi {
        return FixedTimeRowUi(
            id = id,
            timeInput = ""
        )
    }

    fun newAnchoredTimeRow(id: String = generateRowId()): AnchoredTimeRowUi {
        return AnchoredTimeRowUi(
            id = id,
            anchor = AnchorTypeUi.WAKE_UP,
            offsetMinutesInput = "0"
        )
    }

    /**
     * Placeholder for the next step:
     * map a validated parsed draft to the real domain ScheduleRule.
     *
     * Once you give me the actual domain model file(s), I can replace this with
     * an exact, compile-safe implementation against your existing engine.
     */
    fun toDomainDraft(state: ScheduleEditorState): DomainDraftResult {
        return when (val parsed = parse(state)) {
            is ParseResult.Invalid -> DomainDraftResult.Invalid(parsed.errors)
            is ParseResult.Valid -> DomainDraftResult.Ready(parsed.draft)
        }
    }

    private fun parseTimeInput(input: String): ParsedTimeOfDay? {
        val trimmed = input.trim()
        val match = TIME_REGEX.matchEntire(trimmed) ?: return null

        val hour = match.groupValues[1].toIntOrNull() ?: return null
        val minute = match.groupValues[2].toIntOrNull() ?: return null

        if (hour !in 0..23) return null
        if (minute !in 0..59) return null

        return ParsedTimeOfDay(hour = hour, minute = minute)
    }

    private fun generateRowId(): String = "row_${nextId++}"

    private var nextId: Long = 1L

    private val TIME_REGEX = Regex("""^\s*(\d{1,2}):(\d{2})\s*$""")
}

/**
 * Result of parsing raw UI state.
 */
sealed class ParseResult {
    data class Valid(val draft: ParsedScheduleEditorDraft) : ParseResult()
    data class Invalid(val errors: List<ScheduleValidationError>) : ParseResult()
}

/**
 * Result exposed to the future domain bridge step.
 *
 * We name it separately so the VM/editor integration can stay stable even when
 * the exact domain mapping implementation is filled in later.
 */
sealed class DomainDraftResult {
    data class Ready(val parsed: ParsedScheduleEditorDraft) : DomainDraftResult()
    data class Invalid(val errors: List<ScheduleValidationError>) : DomainDraftResult()
}

/**
 * Typed, validated schedule editor draft.
 *
 * This is the safest boundary object between raw UI text state and the
 * project's existing scheduling domain engine.
 */
data class ParsedScheduleEditorDraft(
    val isEnabled: Boolean,
    val recurrenceMode: RecurrenceMode,
    val interval: Int,
    val selectedWeekdays: Set<WeekdayUi>,
    val startDate: LocalDate,
    val hasEndDate: Boolean,
    val endDate: LocalDate?,
    val timingMode: TimingMode,
    val fixedTimes: List<ParsedFixedTimeRow>,
    val anchoredTimes: List<ParsedAnchoredTimeRow>
)

data class ParsedFixedTimeRow(
    val id: String,
    val hour: Int,
    val minute: Int
)

data class ParsedAnchoredTimeRow(
    val id: String,
    val anchor: AnchorTypeUi,
    val offsetMinutes: Int
)

private data class ParsedTimeOfDay(
    val hour: Int,
    val minute: Int
)