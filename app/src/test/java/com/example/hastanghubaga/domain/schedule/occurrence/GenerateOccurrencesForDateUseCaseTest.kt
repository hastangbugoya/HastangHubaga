package com.example.hastanghubaga.domain.schedule.occurrence

import com.example.hastanghubaga.domain.schedule.model.AnchorTimeContext
import com.example.hastanghubaga.domain.schedule.model.AnchoredTimeSpec
import com.example.hastanghubaga.domain.schedule.model.RecurrencePattern
import com.example.hastanghubaga.domain.schedule.model.RecurrenceWindow
import com.example.hastanghubaga.domain.schedule.model.ScheduleRule
import com.example.hastanghubaga.domain.schedule.model.ScheduleTiming
import com.example.hastanghubaga.domain.schedule.model.TimeAnchor
import com.example.hastanghubaga.domain.schedule.model.TimeOfDaySpec
import com.example.hastanghubaga.domain.schedule.recurrence.IsScheduledOnDateUseCase
import com.example.hastanghubaga.domain.schedule.timing.ApplyAnchorOffsetUseCase
import com.example.hastanghubaga.domain.schedule.timing.ResolveAnchorTimeUseCase
import com.example.hastanghubaga.domain.schedule.timing.ResolveScheduleTimesForDateUseCase
import com.example.hastanghubaga.domain.schedule.timing.SortResolvedScheduleTimesUseCase
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GenerateOccurrencesForDateUseCaseTest {

    private val useCase = GenerateOccurrencesForDateUseCase(
        isScheduledOnDateUseCase = IsScheduledOnDateUseCase(),
        resolveScheduleTimesForDateUseCase = ResolveScheduleTimesForDateUseCase(
            resolveAnchorTimeUseCase = ResolveAnchorTimeUseCase(),
            applyAnchorOffsetUseCase = ApplyAnchorOffsetUseCase(),
            sortResolvedScheduleTimesUseCase = SortResolvedScheduleTimesUseCase()
        )
    )

    @Test
    fun inactive_date_returns_empty() {
        val rule = ScheduleRule(
            recurrence = RecurrencePattern.Weekly(
                intervalWeeks = 1,
                daysOfWeek = setOf(DayOfWeek.MONDAY)
            ),
            timing = ScheduleTiming.FixedTimes(
                times = listOf(TimeOfDaySpec(LocalTime(8, 0)))
            ),
            window = RecurrenceWindow(LocalDate(2026, 4, 1))
        )

        val result = useCase(
            rule = rule,
            date = LocalDate(2026, 4, 2) // Thursday
        )

        assertTrue(result.isEmpty())
    }

    @Test
    fun active_fixed_time_rule_generates_occurrences_for_each_time() {
        val date = LocalDate(2026, 4, 2)

        val rule = ScheduleRule(
            recurrence = RecurrencePattern.Daily(1),
            timing = ScheduleTiming.FixedTimes(
                times = listOf(
                    TimeOfDaySpec(LocalTime(18, 0), label = "Evening"),
                    TimeOfDaySpec(LocalTime(8, 0), label = "Morning")
                )
            ),
            window = RecurrenceWindow(LocalDate(2026, 4, 1))
        )

        val result = useCase(
            rule = rule,
            date = date
        )

        assertEquals(
            listOf(LocalTime(8, 0), LocalTime(18, 0)),
            result.map { it.time }
        )
        assertEquals(
            listOf("Morning", "Evening"),
            result.map { it.label }
        )
        assertEquals(date, result[0].key.date)
        assertEquals(LocalTime(8, 0), result[0].key.time)
    }

    @Test
    fun active_anchored_rule_generates_occurrences_when_context_is_available() {
        val date = LocalDate(2026, 4, 2)

        val rule = ScheduleRule(
            recurrence = RecurrencePattern.Daily(1),
            timing = ScheduleTiming.AnchoredTimes(
                occurrences = listOf(
                    AnchoredTimeSpec(TimeAnchor.BREAKFAST, offsetMinutes = 30, label = "Breakfast Plus"),
                    AnchoredTimeSpec(TimeAnchor.DINNER, offsetMinutes = -15, label = "Pre Dinner")
                )
            ),
            window = RecurrenceWindow(LocalDate(2026, 4, 1))
        )

        val context = AnchorTimeContext(
            date = date,
            defaultTimes = mapOf(
                TimeAnchor.BREAKFAST to LocalTime(8, 0),
                TimeAnchor.DINNER to LocalTime(18, 0)
            )
        )

        val result = useCase(
            rule = rule,
            date = date,
            anchorContextProvider = { context }
        )

        assertEquals(
            listOf(LocalTime(8, 30), LocalTime(17, 45)),
            result.map { it.time }
        )
        assertEquals(
            listOf("Breakfast Plus", "Pre Dinner"),
            result.map { it.label }
        )
    }

    @Test
    fun anchored_rule_without_context_returns_empty() {
        val rule = ScheduleRule(
            recurrence = RecurrencePattern.Daily(1),
            timing = ScheduleTiming.AnchoredTimes(
                occurrences = listOf(
                    AnchoredTimeSpec(TimeAnchor.BREAKFAST)
                )
            ),
            window = RecurrenceWindow(LocalDate(2026, 4, 1))
        )

        val result = useCase(
            rule = rule,
            date = LocalDate(2026, 4, 2),
            anchorContextProvider = null
        )

        assertTrue(result.isEmpty())
    }

    @Test
    fun anchored_rule_with_missing_anchor_returns_partial_results() {
        val date = LocalDate(2026, 4, 2)

        val rule = ScheduleRule(
            recurrence = RecurrencePattern.Daily(1),
            timing = ScheduleTiming.AnchoredTimes(
                occurrences = listOf(
                    AnchoredTimeSpec(TimeAnchor.BREAKFAST, label = "Breakfast"),
                    AnchoredTimeSpec(TimeAnchor.DINNER, label = "Dinner")
                )
            ),
            window = RecurrenceWindow(LocalDate(2026, 4, 1))
        )

        val context = AnchorTimeContext(
            date = date,
            defaultTimes = mapOf(
                TimeAnchor.BREAKFAST to LocalTime(8, 0)
            )
        )

        val result = useCase(
            rule = rule,
            date = date,
            anchorContextProvider = { context }
        )

        assertEquals(1, result.size)
        assertEquals(LocalTime(8, 0), result.first().time)
        assertEquals("Breakfast", result.first().label)
    }
}