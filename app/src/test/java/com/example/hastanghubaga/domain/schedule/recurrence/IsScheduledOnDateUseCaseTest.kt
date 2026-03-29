package com.example.hastanghubaga.domain.schedule.recurrence

import com.example.hastanghubaga.domain.schedule.model.RecurrencePattern
import com.example.hastanghubaga.domain.schedule.model.RecurrenceWindow
import com.example.hastanghubaga.domain.schedule.model.ScheduleRule
import com.example.hastanghubaga.domain.schedule.model.ScheduleTiming
import com.example.hastanghubaga.domain.schedule.model.TimeOfDaySpec
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class IsScheduledOnDateUseCaseTest {

    private val useCase = IsScheduledOnDateUseCase()

    @Test
    fun disabled_rule_returns_false() {
        val rule = dailyRule(
            startDate = LocalDate(2026, 4, 1),
            isEnabled = false
        )

        val result = useCase(
            rule = rule,
            date = LocalDate(2026, 4, 1)
        )

        assertFalse(result)
    }

    @Test
    fun date_before_start_returns_false() {
        val rule = dailyRule(
            startDate = LocalDate(2026, 4, 10)
        )

        val result = useCase(
            rule = rule,
            date = LocalDate(2026, 4, 9)
        )

        assertFalse(result)
    }

    @Test
    fun date_equal_to_start_returns_true_for_daily_interval_1() {
        val rule = dailyRule(
            startDate = LocalDate(2026, 4, 10)
        )

        val result = useCase(
            rule = rule,
            date = LocalDate(2026, 4, 10)
        )

        assertTrue(result)
    }

    @Test
    fun end_date_is_inclusive() {
        val rule = dailyRule(
            startDate = LocalDate(2026, 4, 1),
            endDateInclusive = LocalDate(2026, 4, 5)
        )

        val result = useCase(
            rule = rule,
            date = LocalDate(2026, 4, 5)
        )

        assertTrue(result)
    }

    @Test
    fun date_after_end_returns_false() {
        val rule = dailyRule(
            startDate = LocalDate(2026, 4, 1),
            endDateInclusive = LocalDate(2026, 4, 5)
        )

        val result = useCase(
            rule = rule,
            date = LocalDate(2026, 4, 6)
        )

        assertFalse(result)
    }

    @Test
    fun daily_interval_2_matches_every_other_day_from_start() {
        val rule = dailyRule(
            startDate = LocalDate(2026, 4, 1),
            intervalDays = 2
        )

        assertTrue(useCase(rule, LocalDate(2026, 4, 1)))
        assertFalse(useCase(rule, LocalDate(2026, 4, 2)))
        assertTrue(useCase(rule, LocalDate(2026, 4, 3)))
        assertFalse(useCase(rule, LocalDate(2026, 4, 4)))
        assertTrue(useCase(rule, LocalDate(2026, 4, 5)))
    }

    @Test
    fun daily_interval_3_is_anchored_to_start_date() {
        val rule = dailyRule(
            startDate = LocalDate(2026, 4, 2),
            intervalDays = 3
        )

        assertTrue(useCase(rule, LocalDate(2026, 4, 2)))
        assertFalse(useCase(rule, LocalDate(2026, 4, 3)))
        assertFalse(useCase(rule, LocalDate(2026, 4, 4)))
        assertTrue(useCase(rule, LocalDate(2026, 4, 5)))
    }

    @Test
    fun invalid_daily_interval_returns_false_defensively() {
        val rule = dailyRule(
            startDate = LocalDate(2026, 4, 1),
            intervalDays = 0
        )

        val result = useCase(
            rule = rule,
            date = LocalDate(2026, 4, 1)
        )

        assertFalse(result)
    }

    @Test
    fun weekly_rule_matches_selected_day_in_start_week_on_or_after_start() {
        val rule = weeklyRule(
            startDate = LocalDate(2026, 4, 1), // Wednesday
            intervalWeeks = 1,
            daysOfWeek = setOf(DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
        )

        assertTrue(useCase(rule, LocalDate(2026, 4, 1))) // Wed
        assertTrue(useCase(rule, LocalDate(2026, 4, 3))) // Fri
    }

    @Test
    fun weekly_rule_rejects_unselected_day() {
        val rule = weeklyRule(
            startDate = LocalDate(2026, 4, 1), // Wednesday
            intervalWeeks = 1,
            daysOfWeek = setOf(DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
        )

        val result = useCase(
            rule = rule,
            date = LocalDate(2026, 4, 2) // Thursday
        )

        assertFalse(result)
    }

    @Test
    fun weekly_interval_2_is_anchored_to_week_containing_start_date() {
        val rule = weeklyRule(
            startDate = LocalDate(2026, 4, 1), // Wednesday
            intervalWeeks = 2,
            daysOfWeek = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
        )

        assertTrue(useCase(rule, LocalDate(2026, 4, 1)))  // Wed, start week
        assertTrue(useCase(rule, LocalDate(2026, 4, 3)))  // Fri, start week

        assertFalse(useCase(rule, LocalDate(2026, 4, 8))) // Wed, next week
        assertFalse(useCase(rule, LocalDate(2026, 4, 10))) // Fri, next week

        assertTrue(useCase(rule, LocalDate(2026, 4, 13))) // Mon, two weeks from start week
        assertTrue(useCase(rule, LocalDate(2026, 4, 15))) // Wed, two weeks from start week
    }

    @Test
    fun weekly_rule_does_not_include_date_before_start_even_if_in_same_week() {
        val rule = weeklyRule(
            startDate = LocalDate(2026, 4, 1), // Wednesday
            intervalWeeks = 1,
            daysOfWeek = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
        )

        val result = useCase(
            rule = rule,
            date = LocalDate(2026, 3, 30) // Monday in same anchored week, but before start
        )

        assertFalse(result)
    }

    @Test
    fun invalid_weekly_interval_returns_false_defensively() {
        val rule = weeklyRule(
            startDate = LocalDate(2026, 4, 1),
            intervalWeeks = 0,
            daysOfWeek = setOf(DayOfWeek.WEDNESDAY)
        )

        val result = useCase(
            rule = rule,
            date = LocalDate(2026, 4, 1)
        )

        assertFalse(result)
    }

    @Test
    fun weekly_with_empty_days_returns_false_defensively() {
        val rule = weeklyRule(
            startDate = LocalDate(2026, 4, 1),
            intervalWeeks = 1,
            daysOfWeek = emptySet()
        )

        val result = useCase(
            rule = rule,
            date = LocalDate(2026, 4, 1)
        )

        assertFalse(result)
    }

    private fun dailyRule(
        startDate: LocalDate,
        intervalDays: Int = 1,
        endDateInclusive: LocalDate? = null,
        isEnabled: Boolean = true
    ): ScheduleRule {
        return ScheduleRule(
            recurrence = RecurrencePattern.Daily(intervalDays = intervalDays),
            timing = ScheduleTiming.FixedTimes(
                times = listOf(TimeOfDaySpec(LocalTime(8, 0)))
            ),
            window = RecurrenceWindow(
                startDate = startDate,
                endDateInclusive = endDateInclusive
            ),
            isEnabled = isEnabled
        )
    }

    private fun weeklyRule(
        startDate: LocalDate,
        intervalWeeks: Int = 1,
        daysOfWeek: Set<DayOfWeek>,
        endDateInclusive: LocalDate? = null,
        isEnabled: Boolean = true
    ): ScheduleRule {
        return ScheduleRule(
            recurrence = RecurrencePattern.Weekly(
                intervalWeeks = intervalWeeks,
                daysOfWeek = daysOfWeek
            ),
            timing = ScheduleTiming.FixedTimes(
                times = listOf(TimeOfDaySpec(LocalTime(8, 0)))
            ),
            window = RecurrenceWindow(
                startDate = startDate,
                endDateInclusive = endDateInclusive
            ),
            isEnabled = isEnabled
        )
    }
}