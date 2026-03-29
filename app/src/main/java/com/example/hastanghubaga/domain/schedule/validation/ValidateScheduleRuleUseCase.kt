package com.example.hastanghubaga.domain.schedule.validation

import com.example.hastanghubaga.domain.schedule.model.RecurrencePattern
import com.example.hastanghubaga.domain.schedule.model.ScheduleRule
import com.example.hastanghubaga.domain.schedule.model.ScheduleTiming

class ValidateScheduleRuleUseCase {

    operator fun invoke(
        rule: ScheduleRule
    ): ScheduleValidationResult {
        val errors = buildList {
            validateWindow(rule)?.let(::add)
            validateRecurrence(rule)?.let(::add)
            addAll(validateTiming(rule))
        }

        return if (errors.isEmpty()) {
            ScheduleValidationResult.Valid
        } else {
            ScheduleValidationResult.Invalid(errors)
        }
    }

    private fun validateWindow(
        rule: ScheduleRule
    ): ScheduleValidationError? {
        val endDate = rule.window.endDateInclusive ?: return null
        return if (endDate < rule.window.startDate) {
            ScheduleValidationError.END_BEFORE_START
        } else {
            null
        }
    }

    private fun validateRecurrence(
        rule: ScheduleRule
    ): ScheduleValidationError? {
        return when (val recurrence = rule.recurrence) {
            is RecurrencePattern.Daily -> {
                if (recurrence.intervalDays < 1) {
                    ScheduleValidationError.INVALID_DAILY_INTERVAL
                } else {
                    null
                }
            }

            is RecurrencePattern.Weekly -> {
                when {
                    recurrence.intervalWeeks < 1 -> {
                        ScheduleValidationError.INVALID_WEEKLY_INTERVAL
                    }

                    recurrence.daysOfWeek.isEmpty() -> {
                        ScheduleValidationError.WEEKLY_DAYS_EMPTY
                    }

                    else -> null
                }
            }
        }
    }

    private fun validateTiming(
        rule: ScheduleRule
    ): List<ScheduleValidationError> {
        return when (val timing = rule.timing) {
            is ScheduleTiming.FixedTimes -> {
                buildList {
                    if (timing.times.isEmpty()) {
                        add(ScheduleValidationError.FIXED_TIMES_EMPTY)
                    }

                    val duplicateTimesExist = timing.times
                        .map { it.time }
                        .groupingBy { it }
                        .eachCount()
                        .values
                        .any { it > 1 }

                    if (duplicateTimesExist) {
                        add(ScheduleValidationError.FIXED_TIMES_DUPLICATE)
                    }
                }
            }

            is ScheduleTiming.AnchoredTimes -> {
                buildList {
                    if (timing.occurrences.isEmpty()) {
                        add(ScheduleValidationError.ANCHORED_TIMES_EMPTY)
                    }
                }
            }
        }
    }
}
