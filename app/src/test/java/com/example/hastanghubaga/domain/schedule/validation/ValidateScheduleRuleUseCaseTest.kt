package com.example.hastanghubaga.domain.schedule.validation

import com.example.hastanghubaga.domain.schedule.model.AnchoredTimeSpec
import com.example.hastanghubaga.domain.schedule.model.RecurrencePattern
import com.example.hastanghubaga.domain.schedule.model.RecurrenceWindow
import com.example.hastanghubaga.domain.schedule.model.ScheduleRule
import com.example.hastanghubaga.domain.schedule.model.ScheduleTiming
import com.example.hastanghubaga.domain.schedule.model.TimeAnchor
import com.example.hastanghubaga.domain.schedule.model.TimeOfDaySpec
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidateScheduleRuleUseCaseTest {

    private val useCase = ValidateScheduleRuleUseCase()

    @Test
    fun valid_daily_fixed_time_rule_returns_valid() {
        val rule = ScheduleRule(
            recurrence = RecurrencePattern.Daily(intervalDays = 1),
            timing = ScheduleTiming.FixedTimes(
                times = listOf(
                    TimeOfDaySpec(LocalTime(8, 0))
                )
            ),
            window = RecurrenceWindow(
                startDate = LocalDate(2026, 4, 1)
            ),
            isEnabled = true
        )

        val result = useCase(rule)

        assertEquals(ScheduleValidationResult.Valid, result)
    }

    @Test
    fun valid_weekly_rule_with_days_returns_valid() {
        val rule = ScheduleRule(
            recurrence = RecurrencePattern.Weekly(
                intervalWeeks = 1,
                daysOfWeek = setOf(
                    DayOfWeek.MONDAY,
                    DayOfWeek.WEDNESDAY,
                    DayOfWeek.FRIDAY
                )
            ),
            timing = ScheduleTiming.FixedTimes(
                times = listOf(
                    TimeOfDaySpec(LocalTime(7, 30))
                )
            ),
            window = RecurrenceWindow(
                startDate = LocalDate(2026, 4, 1)
            ),
            isEnabled = true
        )

        val result = useCase(rule)

        assertEquals(ScheduleValidationResult.Valid, result)
    }

    @Test
    fun daily_interval_less_than_1_returns_invalid_daily_interval() {
        val rule = ScheduleRule(
            recurrence = RecurrencePattern.Daily(intervalDays = 0),
            timing = ScheduleTiming.FixedTimes(
                times = listOf(
                    TimeOfDaySpec(LocalTime(8, 0))
                )
            ),
            window = RecurrenceWindow(
                startDate = LocalDate(2026, 4, 1)
            )
        )

        val result = useCase(rule)

        assertInvalidWithExactly(
            result = result,
            expectedErrors = listOf(ScheduleValidationError.INVALID_DAILY_INTERVAL)
        )
    }

    @Test
    fun weekly_interval_less_than_1_returns_invalid_weekly_interval() {
        val rule = ScheduleRule(
            recurrence = RecurrencePattern.Weekly(
                intervalWeeks = 0,
                daysOfWeek = setOf(DayOfWeek.MONDAY)
            ),
            timing = ScheduleTiming.FixedTimes(
                times = listOf(
                    TimeOfDaySpec(LocalTime(8, 0))
                )
            ),
            window = RecurrenceWindow(
                startDate = LocalDate(2026, 4, 1)
            )
        )

        val result = useCase(rule)

        assertInvalidWithExactly(
            result = result,
            expectedErrors = listOf(ScheduleValidationError.INVALID_WEEKLY_INTERVAL)
        )
    }

    @Test
    fun weekly_with_empty_days_returns_weekly_days_empty() {
        val rule = ScheduleRule(
            recurrence = RecurrencePattern.Weekly(
                intervalWeeks = 1,
                daysOfWeek = emptySet()
            ),
            timing = ScheduleTiming.FixedTimes(
                times = listOf(
                    TimeOfDaySpec(LocalTime(8, 0))
                )
            ),
            window = RecurrenceWindow(
                startDate = LocalDate(2026, 4, 1)
            )
        )

        val result = useCase(rule)

        assertInvalidWithExactly(
            result = result,
            expectedErrors = listOf(ScheduleValidationError.WEEKLY_DAYS_EMPTY)
        )
    }

    @Test
    fun end_before_start_returns_end_before_start() {
        val rule = ScheduleRule(
            recurrence = RecurrencePattern.Daily(intervalDays = 1),
            timing = ScheduleTiming.FixedTimes(
                times = listOf(
                    TimeOfDaySpec(LocalTime(8, 0))
                )
            ),
            window = RecurrenceWindow(
                startDate = LocalDate(2026, 4, 10),
                endDateInclusive = LocalDate(2026, 4, 9)
            )
        )

        val result = useCase(rule)

        assertInvalidWithExactly(
            result = result,
            expectedErrors = listOf(ScheduleValidationError.END_BEFORE_START)
        )
    }

    @Test
    fun fixed_times_empty_returns_fixed_times_empty() {
        val rule = ScheduleRule(
            recurrence = RecurrencePattern.Daily(intervalDays = 1),
            timing = ScheduleTiming.FixedTimes(
                times = emptyList()
            ),
            window = RecurrenceWindow(
                startDate = LocalDate(2026, 4, 1)
            )
        )

        val result = useCase(rule)

        assertInvalidWithExactly(
            result = result,
            expectedErrors = listOf(ScheduleValidationError.FIXED_TIMES_EMPTY)
        )
    }

    @Test
    fun fixed_times_duplicate_returns_fixed_times_duplicate() {
        val rule = ScheduleRule(
            recurrence = RecurrencePattern.Daily(intervalDays = 1),
            timing = ScheduleTiming.FixedTimes(
                times = listOf(
                    TimeOfDaySpec(LocalTime(8, 0)),
                    TimeOfDaySpec(LocalTime(8, 0))
                )
            ),
            window = RecurrenceWindow(
                startDate = LocalDate(2026, 4, 1)
            )
        )

        val result = useCase(rule)

        assertInvalidWithExactly(
            result = result,
            expectedErrors = listOf(ScheduleValidationError.FIXED_TIMES_DUPLICATE)
        )
    }

    @Test
    fun anchored_times_empty_returns_anchored_times_empty() {
        val rule = ScheduleRule(
            recurrence = RecurrencePattern.Daily(intervalDays = 1),
            timing = ScheduleTiming.AnchoredTimes(
                occurrences = emptyList()
            ),
            window = RecurrenceWindow(
                startDate = LocalDate(2026, 4, 1)
            )
        )

        val result = useCase(rule)

        assertInvalidWithExactly(
            result = result,
            expectedErrors = listOf(ScheduleValidationError.ANCHORED_TIMES_EMPTY)
        )
    }

    @Test
    fun valid_anchored_rule_returns_valid() {
        val rule = ScheduleRule(
            recurrence = RecurrencePattern.Weekly(
                intervalWeeks = 1,
                daysOfWeek = setOf(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY)
            ),
            timing = ScheduleTiming.AnchoredTimes(
                occurrences = listOf(
                    AnchoredTimeSpec(
                        anchor = TimeAnchor.BREAKFAST,
                        offsetMinutes = 15,
                        label = "Morning"
                    )
                )
            ),
            window = RecurrenceWindow(
                startDate = LocalDate(2026, 4, 1)
            )
        )

        val result = useCase(rule)

        assertEquals(ScheduleValidationResult.Valid, result)
    }

    @Test
    fun multiple_invalid_parts_return_multiple_errors() {
        val rule = ScheduleRule(
            recurrence = RecurrencePattern.Weekly(
                intervalWeeks = 0,
                daysOfWeek = emptySet()
            ),
            timing = ScheduleTiming.FixedTimes(
                times = emptyList()
            ),
            window = RecurrenceWindow(
                startDate = LocalDate(2026, 4, 10),
                endDateInclusive = LocalDate(2026, 4, 1)
            )
        )

        val result = useCase(rule)

        assertTrue(result is ScheduleValidationResult.Invalid)
        val errors = (result as ScheduleValidationResult.Invalid).errors

        assertEquals(
            listOf(
                ScheduleValidationError.END_BEFORE_START,
                ScheduleValidationError.INVALID_WEEKLY_INTERVAL,
                ScheduleValidationError.FIXED_TIMES_EMPTY
            ),
            errors
        )
    }

    private fun assertInvalidWithExactly(
        result: ScheduleValidationResult,
        expectedErrors: List<ScheduleValidationError>
    ) {
        assertTrue(result is ScheduleValidationResult.Invalid)
        assertEquals(expectedErrors, (result as ScheduleValidationResult.Invalid).errors)
    }
}