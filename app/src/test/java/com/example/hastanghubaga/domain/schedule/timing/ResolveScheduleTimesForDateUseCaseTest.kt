package com.example.hastanghubaga.domain.schedule.timing

import com.example.hastanghubaga.domain.schedule.model.AnchorTimeContext
import com.example.hastanghubaga.domain.schedule.model.AnchoredTimeSpec
import com.example.hastanghubaga.domain.schedule.model.RecurrencePattern
import com.example.hastanghubaga.domain.schedule.model.RecurrenceWindow
import com.example.hastanghubaga.domain.schedule.model.ScheduleRule
import com.example.hastanghubaga.domain.schedule.model.ScheduleTiming
import com.example.hastanghubaga.domain.schedule.model.TimeAnchor
import com.example.hastanghubaga.domain.schedule.model.TimeOfDaySpec
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ResolveScheduleTimesForDateUseCaseTest {

    private val resolveAnchorTimeUseCase = ResolveAnchorTimeUseCase()
    private val applyAnchorOffsetUseCase = ApplyAnchorOffsetUseCase()
    private val sortResolvedScheduleTimesUseCase = SortResolvedScheduleTimesUseCase()

    private val useCase = ResolveScheduleTimesForDateUseCase(
        resolveAnchorTimeUseCase = resolveAnchorTimeUseCase,
        applyAnchorOffsetUseCase = applyAnchorOffsetUseCase,
        sortResolvedScheduleTimesUseCase = sortResolvedScheduleTimesUseCase
    )

    @Test
    fun fixed_times_are_passed_through_and_sorted() {
        val rule = ScheduleRule(
            recurrence = RecurrencePattern.Daily(1),
            timing = ScheduleTiming.FixedTimes(
                times = listOf(
                    TimeOfDaySpec(LocalTime(10, 0)),
                    TimeOfDaySpec(LocalTime(8, 0)),
                    TimeOfDaySpec(LocalTime(9, 0))
                )
            ),
            window = RecurrenceWindow(LocalDate(2026, 4, 1))
        )

        val result = useCase(
            rule = rule,
            date = LocalDate(2026, 4, 2)
        )

        assertEquals(
            listOf(
                LocalTime(8, 0),
                LocalTime(9, 0),
                LocalTime(10, 0)
            ),
            result.map { it.time }
        )
    }

    @Test
    fun anchored_times_resolve_with_default_anchor_times() {
        val date = LocalDate(2026, 4, 2)

        val rule = ScheduleRule(
            recurrence = RecurrencePattern.Daily(1),
            timing = ScheduleTiming.AnchoredTimes(
                occurrences = listOf(
                    AnchoredTimeSpec(TimeAnchor.BREAKFAST),
                    AnchoredTimeSpec(TimeAnchor.DINNER)
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
            listOf(
                LocalTime(8, 0),
                LocalTime(18, 0)
            ),
            result.map { it.time }
        )
    }

    @Test
    fun anchored_times_apply_offsets_correctly() {
        val date = LocalDate(2026, 4, 2)

        val rule = ScheduleRule(
            recurrence = RecurrencePattern.Daily(1),
            timing = ScheduleTiming.AnchoredTimes(
                occurrences = listOf(
                    AnchoredTimeSpec(TimeAnchor.BREAKFAST, offsetMinutes = 30),
                    AnchoredTimeSpec(TimeAnchor.DINNER, offsetMinutes = -15)
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
            listOf(
                LocalTime(8, 30),
                LocalTime(17, 45)
            ),
            result.map { it.time }
        )
    }

    @Test
    fun missing_anchor_context_returns_empty_list() {
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
    fun missing_individual_anchor_is_skipped_not_failed() {
        val date = LocalDate(2026, 4, 2)

        val rule = ScheduleRule(
            recurrence = RecurrencePattern.Daily(1),
            timing = ScheduleTiming.AnchoredTimes(
                occurrences = listOf(
                    AnchoredTimeSpec(TimeAnchor.BREAKFAST),
                    AnchoredTimeSpec(TimeAnchor.DINNER)
                )
            ),
            window = RecurrenceWindow(LocalDate(2026, 4, 1))
        )

        val context = AnchorTimeContext(
            date = date,
            defaultTimes = mapOf(
                TimeAnchor.BREAKFAST to LocalTime(8, 0)
                // DINNER missing
            )
        )

        val result = useCase(
            rule = rule,
            date = date,
            anchorContextProvider = { context }
        )

        assertEquals(
            listOf(LocalTime(8, 0)),
            result.map { it.time }
        )
    }

    @Test
    fun result_is_sorted_after_resolution() {
        val date = LocalDate(2026, 4, 2)

        val rule = ScheduleRule(
            recurrence = RecurrencePattern.Daily(1),
            timing = ScheduleTiming.AnchoredTimes(
                occurrences = listOf(
                    AnchoredTimeSpec(TimeAnchor.DINNER),
                    AnchoredTimeSpec(TimeAnchor.BREAKFAST)
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
            listOf(
                LocalTime(8, 0),
                LocalTime(18, 0)
            ),
            result.map { it.time }
        )
    }
}