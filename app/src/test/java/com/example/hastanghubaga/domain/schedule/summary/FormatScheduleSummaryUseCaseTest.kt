package com.example.hastanghubaga.domain.schedule.summary

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
import org.junit.Test

class FormatScheduleSummaryUseCaseTest {

    private val useCase = FormatScheduleSummaryUseCase()

    @Test
    fun daily_interval_1_with_single_fixed_time_formats_correctly() {
        val rule = ScheduleRule(
            recurrence = RecurrencePattern.Daily(intervalDays = 1),
            timing = ScheduleTiming.FixedTimes(
                times = listOf(
                    TimeOfDaySpec(LocalTime(8, 0))
                )
            ),
            window = RecurrenceWindow(LocalDate(2026, 4, 1))
        )

        val result = useCase(rule)

        assertEquals("Daily at 8:00 AM", result)
    }

    @Test
    fun daily_interval_greater_than_1_with_multiple_fixed_times_formats_correctly() {
        val rule = ScheduleRule(
            recurrence = RecurrencePattern.Daily(intervalDays = 2),
            timing = ScheduleTiming.FixedTimes(
                times = listOf(
                    TimeOfDaySpec(LocalTime(18, 0)),
                    TimeOfDaySpec(LocalTime(8, 0))
                )
            ),
            window = RecurrenceWindow(LocalDate(2026, 4, 1))
        )

        val result = useCase(rule)

        assertEquals("Every 2 days at 8:00 AM and 6:00 PM", result)
    }

    @Test
    fun weekly_interval_1_formats_sorted_day_names() {
        val rule = ScheduleRule(
            recurrence = RecurrencePattern.Weekly(
                intervalWeeks = 1,
                daysOfWeek = setOf(
                    DayOfWeek.FRIDAY,
                    DayOfWeek.MONDAY,
                    DayOfWeek.WEDNESDAY
                )
            ),
            timing = ScheduleTiming.FixedTimes(
                times = listOf(
                    TimeOfDaySpec(LocalTime(7, 30))
                )
            ),
            window = RecurrenceWindow(LocalDate(2026, 4, 1))
        )

        val result = useCase(rule)

        assertEquals("Every week on Mon, Wed, Fri at 7:30 AM", result)
    }

    @Test
    fun weekly_interval_greater_than_1_formats_correctly() {
        val rule = ScheduleRule(
            recurrence = RecurrencePattern.Weekly(
                intervalWeeks = 2,
                daysOfWeek = setOf(DayOfWeek.TUESDAY)
            ),
            timing = ScheduleTiming.FixedTimes(
                times = listOf(
                    TimeOfDaySpec(LocalTime(13, 0))
                )
            ),
            window = RecurrenceWindow(LocalDate(2026, 4, 1))
        )

        val result = useCase(rule)

        assertEquals("Every 2 weeks on Tue at 1:00 PM", result)
    }

    @Test
    fun fixed_times_are_sorted_before_formatting() {
        val rule = ScheduleRule(
            recurrence = RecurrencePattern.Daily(intervalDays = 1),
            timing = ScheduleTiming.FixedTimes(
                times = listOf(
                    TimeOfDaySpec(LocalTime(18, 0)),
                    TimeOfDaySpec(LocalTime(12, 0)),
                    TimeOfDaySpec(LocalTime(8, 0))
                )
            ),
            window = RecurrenceWindow(LocalDate(2026, 4, 1))
        )

        val result = useCase(rule)

        assertEquals("Daily at 8:00 AM, 12:00 PM and 6:00 PM", result)
    }

    @Test
    fun anchored_single_zero_offset_formats_anchor_label_only() {
        val rule = ScheduleRule(
            recurrence = RecurrencePattern.Daily(intervalDays = 1),
            timing = ScheduleTiming.AnchoredTimes(
                occurrences = listOf(
                    AnchoredTimeSpec(
                        anchor = TimeAnchor.BREAKFAST,
                        offsetMinutes = 0
                    )
                )
            ),
            window = RecurrenceWindow(LocalDate(2026, 4, 1))
        )

        val result = useCase(rule)

        assertEquals("Daily at breakfast", result)
    }

    @Test
    fun anchored_positive_and_negative_offsets_format_correctly() {
        val rule = ScheduleRule(
            recurrence = RecurrencePattern.Daily(intervalDays = 1),
            timing = ScheduleTiming.AnchoredTimes(
                occurrences = listOf(
                    AnchoredTimeSpec(
                        anchor = TimeAnchor.BREAKFAST,
                        offsetMinutes = 30
                    ),
                    AnchoredTimeSpec(
                        anchor = TimeAnchor.DINNER,
                        offsetMinutes = -15
                    )
                )
            ),
            window = RecurrenceWindow(LocalDate(2026, 4, 1))
        )

        val result = useCase(rule)

        assertEquals("Daily at breakfast + 30m and dinner - 15m", result)
    }

    @Test
    fun anchored_occurrences_are_sorted_by_hint_then_name() {
        val rule = ScheduleRule(
            recurrence = RecurrencePattern.Daily(intervalDays = 1),
            timing = ScheduleTiming.AnchoredTimes(
                occurrences = listOf(
                    AnchoredTimeSpec(
                        anchor = TimeAnchor.DINNER,
                        offsetMinutes = 0,
                        sortOrderHint = 2
                    ),
                    AnchoredTimeSpec(
                        anchor = TimeAnchor.BREAKFAST,
                        offsetMinutes = 0,
                        sortOrderHint = 1
                    )
                )
            ),
            window = RecurrenceWindow(LocalDate(2026, 4, 1))
        )

        val result = useCase(rule)

        assertEquals("Daily at breakfast and dinner", result)
    }

    @Test
    fun fixed_empty_times_returns_recurrence_only() {
        val rule = ScheduleRule(
            recurrence = RecurrencePattern.Daily(intervalDays = 1),
            timing = ScheduleTiming.FixedTimes(
                times = emptyList()
            ),
            window = RecurrenceWindow(LocalDate(2026, 4, 1))
        )

        val result = useCase(rule)

        assertEquals("Daily", result)
    }

    @Test
    fun anchored_empty_occurrences_returns_recurrence_only() {
        val rule = ScheduleRule(
            recurrence = RecurrencePattern.Weekly(
                intervalWeeks = 1,
                daysOfWeek = setOf(DayOfWeek.MONDAY)
            ),
            timing = ScheduleTiming.AnchoredTimes(
                occurrences = emptyList()
            ),
            window = RecurrenceWindow(LocalDate(2026, 4, 1))
        )

        val result = useCase(rule)

        assertEquals("Every week on Mon", result)
    }
}