package com.example.hastanghubaga.data.local.mappers

import com.example.hastanghubaga.data.local.entity.schedule.AnchorDateOverrideTimeEntity
import com.example.hastanghubaga.data.local.entity.schedule.AnchorDayOfWeekTimeEntity
import com.example.hastanghubaga.data.local.entity.schedule.AnchorDefaultTimeEntity
import com.example.hastanghubaga.data.local.entity.schedule.AnchorTimeBundle
import com.example.hastanghubaga.domain.schedule.model.AnchorDateKey
import com.example.hastanghubaga.domain.schedule.model.AnchorDayKey
import com.example.hastanghubaga.domain.schedule.model.AnchorTimeContext
import com.example.hastanghubaga.domain.schedule.model.TimeAnchor
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Test

class AnchorTimeContextMapperTest {

    private val mapper = AnchorTimeContextMapper()

    @Test
    fun toDomain_maps_default_times_correctly() {
        val date = LocalDate(2026, 4, 2)

        val bundle = AnchorTimeBundle(
            defaultTimes = listOf(
                AnchorDefaultTimeEntity(
                    anchor = "BREAKFAST",
                    timeSeconds = 8 * 3600
                ),
                AnchorDefaultTimeEntity(
                    anchor = "DINNER",
                    timeSeconds = 18 * 3600
                )
            ),
            dayOfWeekOverrides = emptyList(),
            dateOverrides = emptyList()
        )

        val result = mapper.toDomain(
            date = date,
            bundle = bundle
        )

        assertEquals(
            AnchorTimeContext(
                date = date,
                defaultTimes = mapOf(
                    TimeAnchor.BREAKFAST to LocalTime(8, 0),
                    TimeAnchor.DINNER to LocalTime(18, 0)
                ),
                dayOfWeekOverrides = emptyMap(),
                dateOverrides = emptyMap()
            ),
            result
        )
    }

    @Test
    fun toDomain_maps_day_of_week_overrides_correctly() {
        val date = LocalDate(2026, 4, 2)

        val bundle = AnchorTimeBundle(
            defaultTimes = emptyList(),
            dayOfWeekOverrides = listOf(
                AnchorDayOfWeekTimeEntity(
                    anchor = "BREAKFAST",
                    dayOfWeek = "MONDAY",
                    timeSeconds = 9 * 3600
                ),
                AnchorDayOfWeekTimeEntity(
                    anchor = "DINNER",
                    dayOfWeek = "FRIDAY",
                    timeSeconds = (18 * 3600) + (30 * 60)
                )
            ),
            dateOverrides = emptyList()
        )

        val result = mapper.toDomain(
            date = date,
            bundle = bundle
        )

        assertEquals(
            mapOf(
                AnchorDayKey(
                    anchor = TimeAnchor.BREAKFAST,
                    dayOfWeek = DayOfWeek.MONDAY
                ) to LocalTime(9, 0),
                AnchorDayKey(
                    anchor = TimeAnchor.DINNER,
                    dayOfWeek = DayOfWeek.FRIDAY
                ) to LocalTime(18, 30)
            ),
            result.dayOfWeekOverrides
        )
    }

    @Test
    fun toDomain_maps_date_overrides_correctly() {
        val date = LocalDate(2026, 4, 2)

        val bundle = AnchorTimeBundle(
            defaultTimes = emptyList(),
            dayOfWeekOverrides = emptyList(),
            dateOverrides = listOf(
                AnchorDateOverrideTimeEntity(
                    anchor = "BREAKFAST",
                    date = "2026-04-02",
                    timeSeconds = (7 * 3600) + (45 * 60)
                ),
                AnchorDateOverrideTimeEntity(
                    anchor = "DINNER",
                    date = "2026-04-03",
                    timeSeconds = 19 * 3600
                )
            )
        )

        val result = mapper.toDomain(
            date = date,
            bundle = bundle
        )

        assertEquals(
            mapOf(
                AnchorDateKey(
                    anchor = TimeAnchor.BREAKFAST,
                    date = LocalDate(2026, 4, 2)
                ) to LocalTime(7, 45),
                AnchorDateKey(
                    anchor = TimeAnchor.DINNER,
                    date = LocalDate(2026, 4, 3)
                ) to LocalTime(19, 0)
            ),
            result.dateOverrides
        )
    }

    @Test
    fun toDomain_maps_full_bundle_correctly() {
        val date = LocalDate(2026, 4, 2)

        val bundle = AnchorTimeBundle(
            defaultTimes = listOf(
                AnchorDefaultTimeEntity(
                    anchor = "BREAKFAST",
                    timeSeconds = 8 * 3600
                )
            ),
            dayOfWeekOverrides = listOf(
                AnchorDayOfWeekTimeEntity(
                    anchor = "BREAKFAST",
                    dayOfWeek = "THURSDAY",
                    timeSeconds = (9 * 3600) + (15 * 60)
                )
            ),
            dateOverrides = listOf(
                AnchorDateOverrideTimeEntity(
                    anchor = "BREAKFAST",
                    date = "2026-04-02",
                    timeSeconds = (10 * 3600) + (30 * 60)
                )
            )
        )

        val result = mapper.toDomain(
            date = date,
            bundle = bundle
        )

        assertEquals(date, result.date)
        assertEquals(
            mapOf(TimeAnchor.BREAKFAST to LocalTime(8, 0)),
            result.defaultTimes
        )
        assertEquals(
            mapOf(
                AnchorDayKey(
                    anchor = TimeAnchor.BREAKFAST,
                    dayOfWeek = DayOfWeek.THURSDAY
                ) to LocalTime(9, 15)
            ),
            result.dayOfWeekOverrides
        )
        assertEquals(
            mapOf(
                AnchorDateKey(
                    anchor = TimeAnchor.BREAKFAST,
                    date = LocalDate(2026, 4, 2)
                ) to LocalTime(10, 30)
            ),
            result.dateOverrides
        )
    }

    @Test
    fun toDomain_handles_empty_bundle() {
        val date = LocalDate(2026, 4, 2)

        val bundle = AnchorTimeBundle(
            defaultTimes = emptyList(),
            dayOfWeekOverrides = emptyList(),
            dateOverrides = emptyList()
        )

        val result = mapper.toDomain(
            date = date,
            bundle = bundle
        )

        assertEquals(
            AnchorTimeContext(
                date = date,
                defaultTimes = emptyMap(),
                dayOfWeekOverrides = emptyMap(),
                dateOverrides = emptyMap()
            ),
            result
        )
    }
}