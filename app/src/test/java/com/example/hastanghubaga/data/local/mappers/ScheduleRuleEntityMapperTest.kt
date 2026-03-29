package com.example.hastanghubaga.data.local.mappers

import com.example.hastanghubaga.data.local.db.mapper.schedule.ScheduleRuleEntityMapper
import com.example.hastanghubaga.data.local.entity.schedule.ScheduleOwnerType
import com.example.hastanghubaga.data.local.entity.schedule.ScheduleRecurrenceType
import com.example.hastanghubaga.data.local.entity.schedule.ScheduleRuleAnchoredTimeEntity
import com.example.hastanghubaga.data.local.entity.schedule.ScheduleRuleEntity
import com.example.hastanghubaga.data.local.entity.schedule.ScheduleRuleFixedTimeEntity
import com.example.hastanghubaga.data.local.entity.schedule.ScheduleRuleWeeklyDayEntity
import com.example.hastanghubaga.data.local.entity.schedule.ScheduleRuleWithDetails
import com.example.hastanghubaga.data.local.entity.schedule.ScheduleTimingType
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

class ScheduleRuleEntityMapperTest {

    private val mapper = ScheduleRuleEntityMapper()

    @Test
    fun toDomain_maps_daily_fixed_rule_correctly() {
        val details = ScheduleRuleWithDetails(
            rule = ScheduleRuleEntity(
                id = 10L,
                ownerType = ScheduleOwnerType.SUPPLEMENT,
                ownerId = 55L,
                recurrenceType = ScheduleRecurrenceType.DAILY,
                intervalValue = 2,
                startDate = "2026-04-01",
                endDateInclusive = "2026-04-30",
                isEnabled = true,
                timingType = ScheduleTimingType.FIXED
            ),
            weeklyDays = emptyList(),
            fixedTimes = listOf(
                ScheduleRuleFixedTimeEntity(
                    id = 1L,
                    scheduleRuleId = 10L,
                    time = "18:00",
                    label = "Evening",
                    sortOrderHint = 2
                ),
                ScheduleRuleFixedTimeEntity(
                    id = 2L,
                    scheduleRuleId = 10L,
                    time = "08:00",
                    label = "Morning",
                    sortOrderHint = 1
                )
            ),
            anchoredTimes = emptyList()
        )

        val result = mapper.toDomain(details)

        assertEquals(
            ScheduleRule(
                recurrence = RecurrencePattern.Daily(intervalDays = 2),
                timing = ScheduleTiming.FixedTimes(
                    times = listOf(
                        TimeOfDaySpec(
                            time = LocalTime.parse("08:00"),
                            label = "Morning",
                            sortOrderHint = 1
                        ),
                        TimeOfDaySpec(
                            time = LocalTime.parse("18:00"),
                            label = "Evening",
                            sortOrderHint = 2
                        )
                    )
                ),
                window = RecurrenceWindow(
                    startDate = LocalDate.parse("2026-04-01"),
                    endDateInclusive = LocalDate.parse("2026-04-30")
                ),
                isEnabled = true
            ),
            result
        )
    }

    @Test
    fun toDomain_maps_weekly_anchored_rule_correctly() {
        val details = ScheduleRuleWithDetails(
            rule = ScheduleRuleEntity(
                id = 20L,
                ownerType = ScheduleOwnerType.ACTIVITY,
                ownerId = 77L,
                recurrenceType = ScheduleRecurrenceType.WEEKLY,
                intervalValue = 1,
                startDate = "2026-04-01",
                endDateInclusive = null,
                isEnabled = false,
                timingType = ScheduleTimingType.ANCHORED
            ),
            weeklyDays = listOf(
                ScheduleRuleWeeklyDayEntity(
                    scheduleRuleId = 20L,
                    dayOfWeek = "FRIDAY"
                ),
                ScheduleRuleWeeklyDayEntity(
                    scheduleRuleId = 20L,
                    dayOfWeek = "MONDAY"
                )
            ),
            fixedTimes = emptyList(),
            anchoredTimes = listOf(
                ScheduleRuleAnchoredTimeEntity(
                    id = 1L,
                    scheduleRuleId = 20L,
                    anchor = "DINNER",
                    offsetMinutes = -15,
                    label = "Before Dinner",
                    sortOrderHint = 2
                ),
                ScheduleRuleAnchoredTimeEntity(
                    id = 2L,
                    scheduleRuleId = 20L,
                    anchor = "BREAKFAST",
                    offsetMinutes = 30,
                    label = "After Breakfast",
                    sortOrderHint = 1
                )
            )
        )

        val result = mapper.toDomain(details)

        assertEquals(
            ScheduleRule(
                recurrence = RecurrencePattern.Weekly(
                    intervalWeeks = 1,
                    daysOfWeek = setOf(
                        DayOfWeek.MONDAY,
                        DayOfWeek.FRIDAY
                    )
                ),
                timing = ScheduleTiming.AnchoredTimes(
                    occurrences = listOf(
                        AnchoredTimeSpec(
                            anchor = TimeAnchor.BREAKFAST,
                            offsetMinutes = 30,
                            label = "After Breakfast",
                            sortOrderHint = 1
                        ),
                        AnchoredTimeSpec(
                            anchor = TimeAnchor.DINNER,
                            offsetMinutes = -15,
                            label = "Before Dinner",
                            sortOrderHint = 2
                        )
                    )
                ),
                window = RecurrenceWindow(
                    startDate = LocalDate.parse("2026-04-01"),
                    endDateInclusive = null
                ),
                isEnabled = false
            ),
            result
        )
    }

    @Test
    fun toRuleEntity_maps_daily_rule_correctly() {
        val rule = ScheduleRule(
            recurrence = RecurrencePattern.Daily(intervalDays = 3),
            timing = ScheduleTiming.FixedTimes(
                times = listOf(
                    TimeOfDaySpec(LocalTime(8, 0))
                )
            ),
            window = RecurrenceWindow(
                startDate = LocalDate(2026, 4, 1),
                endDateInclusive = LocalDate(2026, 4, 30)
            ),
            isEnabled = true
        )

        val result = mapper.toRuleEntity(
            ownerType = ScheduleOwnerType.SUPPLEMENT,
            ownerId = 5L,
            rule = rule
        )

        assertEquals(
            ScheduleRuleEntity(
                id = 0L,
                ownerType = ScheduleOwnerType.SUPPLEMENT,
                ownerId = 5L,
                recurrenceType = ScheduleRecurrenceType.DAILY,
                intervalValue = 3,
                startDate = "2026-04-01",
                endDateInclusive = "2026-04-30",
                isEnabled = true,
                timingType = ScheduleTimingType.FIXED
            ),
            result
        )
    }

    @Test
    fun toRuleEntity_maps_weekly_rule_correctly() {
        val rule = ScheduleRule(
            recurrence = RecurrencePattern.Weekly(
                intervalWeeks = 2,
                daysOfWeek = setOf(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY)
            ),
            timing = ScheduleTiming.AnchoredTimes(
                occurrences = listOf(
                    AnchoredTimeSpec(TimeAnchor.BREAKFAST)
                )
            ),
            window = RecurrenceWindow(
                startDate = LocalDate(2026, 4, 1),
                endDateInclusive = null
            ),
            isEnabled = false
        )

        val result = mapper.toRuleEntity(
            ownerType = ScheduleOwnerType.MEAL_TEMPLATE,
            ownerId = 8L,
            rule = rule
        )

        assertEquals(
            ScheduleRuleEntity(
                id = 0L,
                ownerType = ScheduleOwnerType.MEAL_TEMPLATE,
                ownerId = 8L,
                recurrenceType = ScheduleRecurrenceType.WEEKLY,
                intervalValue = 2,
                startDate = "2026-04-01",
                endDateInclusive = null,
                isEnabled = false,
                timingType = ScheduleTimingType.ANCHORED
            ),
            result
        )
    }

    @Test
    fun toWeeklyDayEntities_returns_sorted_weekdays() {
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
                    TimeOfDaySpec(LocalTime(8, 0))
                )
            ),
            window = RecurrenceWindow(
                startDate = LocalDate(2026, 4, 1)
            )
        )

        val result = mapper.toWeeklyDayEntities(
            scheduleRuleId = 99L,
            rule = rule
        )

        assertEquals(
            listOf(
                ScheduleRuleWeeklyDayEntity(99L, "MONDAY"),
                ScheduleRuleWeeklyDayEntity(99L, "WEDNESDAY"),
                ScheduleRuleWeeklyDayEntity(99L, "FRIDAY")
            ),
            result
        )
    }

    @Test
    fun toWeeklyDayEntities_returns_empty_for_non_weekly_rule() {
        val rule = ScheduleRule(
            recurrence = RecurrencePattern.Daily(intervalDays = 1),
            timing = ScheduleTiming.FixedTimes(
                times = listOf(
                    TimeOfDaySpec(LocalTime(8, 0))
                )
            ),
            window = RecurrenceWindow(LocalDate(2026, 4, 1))
        )

        val result = mapper.toWeeklyDayEntities(
            scheduleRuleId = 99L,
            rule = rule
        )

        assertTrue(result.isEmpty())
    }

    @Test
    fun toFixedTimeEntities_returns_expected_entities() {
        val rule = ScheduleRule(
            recurrence = RecurrencePattern.Daily(intervalDays = 1),
            timing = ScheduleTiming.FixedTimes(
                times = listOf(
                    TimeOfDaySpec(
                        time = LocalTime(8, 0),
                        label = "Morning",
                        sortOrderHint = 1
                    ),
                    TimeOfDaySpec(
                        time = LocalTime(18, 0),
                        label = "Evening",
                        sortOrderHint = 2
                    )
                )
            ),
            window = RecurrenceWindow(LocalDate(2026, 4, 1))
        )

        val result = mapper.toFixedTimeEntities(
            scheduleRuleId = 50L,
            rule = rule
        )

        assertEquals(
            listOf(
                ScheduleRuleFixedTimeEntity(
                    id = 0L,
                    scheduleRuleId = 50L,
                    time = "08:00",
                    label = "Morning",
                    sortOrderHint = 1
                ),
                ScheduleRuleFixedTimeEntity(
                    id = 0L,
                    scheduleRuleId = 50L,
                    time = "18:00",
                    label = "Evening",
                    sortOrderHint = 2
                )
            ),
            result
        )
    }

    @Test
    fun toFixedTimeEntities_returns_empty_for_non_fixed_rule() {
        val rule = ScheduleRule(
            recurrence = RecurrencePattern.Daily(intervalDays = 1),
            timing = ScheduleTiming.AnchoredTimes(
                occurrences = listOf(
                    AnchoredTimeSpec(TimeAnchor.BREAKFAST)
                )
            ),
            window = RecurrenceWindow(LocalDate(2026, 4, 1))
        )

        val result = mapper.toFixedTimeEntities(
            scheduleRuleId = 50L,
            rule = rule
        )

        assertTrue(result.isEmpty())
    }

    @Test
    fun toAnchoredTimeEntities_returns_expected_entities() {
        val rule = ScheduleRule(
            recurrence = RecurrencePattern.Daily(intervalDays = 1),
            timing = ScheduleTiming.AnchoredTimes(
                occurrences = listOf(
                    AnchoredTimeSpec(
                        anchor = TimeAnchor.BREAKFAST,
                        offsetMinutes = 30,
                        label = "After Breakfast",
                        sortOrderHint = 1
                    ),
                    AnchoredTimeSpec(
                        anchor = TimeAnchor.DINNER,
                        offsetMinutes = -15,
                        label = "Before Dinner",
                        sortOrderHint = 2
                    )
                )
            ),
            window = RecurrenceWindow(LocalDate(2026, 4, 1))
        )

        val result = mapper.toAnchoredTimeEntities(
            scheduleRuleId = 60L,
            rule = rule
        )

        assertEquals(
            listOf(
                ScheduleRuleAnchoredTimeEntity(
                    id = 0L,
                    scheduleRuleId = 60L,
                    anchor = "BREAKFAST",
                    offsetMinutes = 30,
                    label = "After Breakfast",
                    sortOrderHint = 1
                ),
                ScheduleRuleAnchoredTimeEntity(
                    id = 0L,
                    scheduleRuleId = 60L,
                    anchor = "DINNER",
                    offsetMinutes = -15,
                    label = "Before Dinner",
                    sortOrderHint = 2
                )
            ),
            result
        )
    }

    @Test
    fun toAnchoredTimeEntities_returns_empty_for_non_anchored_rule() {
        val rule = ScheduleRule(
            recurrence = RecurrencePattern.Daily(intervalDays = 1),
            timing = ScheduleTiming.FixedTimes(
                times = listOf(
                    TimeOfDaySpec(LocalTime(8, 0))
                )
            ),
            window = RecurrenceWindow(LocalDate(2026, 4, 1))
        )

        val result = mapper.toAnchoredTimeEntities(
            scheduleRuleId = 60L,
            rule = rule
        )

        assertTrue(result.isEmpty())
    }
}