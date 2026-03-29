package com.example.hastanghubaga.domain.schedule.timing

import com.example.hastanghubaga.domain.schedule.model.AnchorDateKey
import com.example.hastanghubaga.domain.schedule.model.AnchorDayKey
import com.example.hastanghubaga.domain.schedule.model.AnchorTimeContext
import com.example.hastanghubaga.domain.schedule.model.TimeAnchor
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ResolveAnchorTimeUseCaseTest {

    private val useCase = ResolveAnchorTimeUseCase()

    @Test
    fun returns_date_override_when_present() {
        val date = LocalDate(2026, 4, 8) // Wednesday
        val context = AnchorTimeContext(
            date = date,
            defaultTimes = mapOf(
                TimeAnchor.BREAKFAST to LocalTime(8, 0)
            ),
            dayOfWeekOverrides = mapOf(
                AnchorDayKey(TimeAnchor.BREAKFAST, DayOfWeek.WEDNESDAY) to LocalTime(9, 0)
            ),
            dateOverrides = mapOf(
                AnchorDateKey(TimeAnchor.BREAKFAST, date) to LocalTime(10, 0)
            )
        )

        val result = useCase(
            anchor = TimeAnchor.BREAKFAST,
            context = context
        )

        assertEquals(LocalTime(10, 0), result)
    }

    @Test
    fun returns_day_of_week_override_when_date_override_missing() {
        val date = LocalDate(2026, 4, 8) // Wednesday
        val context = AnchorTimeContext(
            date = date,
            defaultTimes = mapOf(
                TimeAnchor.BREAKFAST to LocalTime(8, 0)
            ),
            dayOfWeekOverrides = mapOf(
                AnchorDayKey(TimeAnchor.BREAKFAST, DayOfWeek.WEDNESDAY) to LocalTime(9, 0)
            ),
            dateOverrides = emptyMap()
        )

        val result = useCase(
            anchor = TimeAnchor.BREAKFAST,
            context = context
        )

        assertEquals(LocalTime(9, 0), result)
    }

    @Test
    fun returns_default_when_no_overrides_exist() {
        val date = LocalDate(2026, 4, 8)
        val context = AnchorTimeContext(
            date = date,
            defaultTimes = mapOf(
                TimeAnchor.BREAKFAST to LocalTime(8, 0)
            ),
            dayOfWeekOverrides = emptyMap(),
            dateOverrides = emptyMap()
        )

        val result = useCase(
            anchor = TimeAnchor.BREAKFAST,
            context = context
        )

        assertEquals(LocalTime(8, 0), result)
    }

    @Test
    fun returns_null_when_anchor_missing_everywhere() {
        val date = LocalDate(2026, 4, 8)
        val context = AnchorTimeContext(
            date = date,
            defaultTimes = emptyMap(),
            dayOfWeekOverrides = emptyMap(),
            dateOverrides = emptyMap()
        )

        val result = useCase(
            anchor = TimeAnchor.BREAKFAST,
            context = context
        )

        assertNull(result)
    }

    @Test
    fun ignores_override_for_different_anchor() {
        val date = LocalDate(2026, 4, 8)
        val context = AnchorTimeContext(
            date = date,
            defaultTimes = mapOf(
                TimeAnchor.BREAKFAST to LocalTime(8, 0)
            ),
            dayOfWeekOverrides = mapOf(
                AnchorDayKey(TimeAnchor.DINNER, DayOfWeek.WEDNESDAY) to LocalTime(18, 30)
            ),
            dateOverrides = mapOf(
                AnchorDateKey(TimeAnchor.DINNER, date) to LocalTime(19, 0)
            )
        )

        val result = useCase(
            anchor = TimeAnchor.BREAKFAST,
            context = context
        )

        assertEquals(LocalTime(8, 0), result)
    }

    @Test
    fun ignores_day_override_for_different_day() {
        val date = LocalDate(2026, 4, 8) // Wednesday
        val context = AnchorTimeContext(
            date = date,
            defaultTimes = mapOf(
                TimeAnchor.BREAKFAST to LocalTime(8, 0)
            ),
            dayOfWeekOverrides = mapOf(
                AnchorDayKey(TimeAnchor.BREAKFAST, DayOfWeek.THURSDAY) to LocalTime(9, 30)
            ),
            dateOverrides = emptyMap()
        )

        val result = useCase(
            anchor = TimeAnchor.BREAKFAST,
            context = context
        )

        assertEquals(LocalTime(8, 0), result)
    }
}