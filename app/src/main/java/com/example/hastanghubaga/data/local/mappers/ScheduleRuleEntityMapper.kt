package com.example.hastanghubaga.data.local.db.mapper.schedule

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
import kotlinx.datetime.isoDayNumber
import javax.inject.Inject

class ScheduleRuleEntityMapper @Inject constructor() {

    fun toDomain(details: ScheduleRuleWithDetails): ScheduleRule {
        val rule = details.rule

        val recurrence = when (rule.recurrenceType) {
            ScheduleRecurrenceType.DAILY -> {
                RecurrencePattern.Daily(
                    intervalDays = rule.intervalValue
                )
            }

            ScheduleRecurrenceType.WEEKLY -> {
                RecurrencePattern.Weekly(
                    intervalWeeks = rule.intervalValue,
                    daysOfWeek = details.weeklyDays
                        .map { DayOfWeek.valueOf(it.dayOfWeek) }
                        .toSet()
                )
            }
        }

        val timing = when (rule.timingType) {
            ScheduleTimingType.FIXED -> {
                ScheduleTiming.FixedTimes(
                    times = details.fixedTimes
                        .sortedWith(
                            compareBy<ScheduleRuleFixedTimeEntity>(
                                { LocalTime.parse(it.time) },
                                { it.sortOrderHint ?: Int.MAX_VALUE },
                                { it.label ?: "" }
                            )
                        )
                        .map { entity ->
                            TimeOfDaySpec(
                                time = LocalTime.parse(entity.time),
                                label = entity.label,
                                sortOrderHint = entity.sortOrderHint
                            )
                        }
                )
            }

            ScheduleTimingType.ANCHORED -> {
                ScheduleTiming.AnchoredTimes(
                    occurrences = details.anchoredTimes
                        .sortedWith(
                            compareBy<ScheduleRuleAnchoredTimeEntity>(
                                { it.sortOrderHint ?: Int.MAX_VALUE },
                                { it.label ?: "" },
                                { it.anchor },
                                { it.offsetMinutes }
                            )
                        )
                        .map { entity ->
                            AnchoredTimeSpec(
                                anchor = TimeAnchor.valueOf(entity.anchor),
                                offsetMinutes = entity.offsetMinutes,
                                label = entity.label,
                                sortOrderHint = entity.sortOrderHint
                            )
                        }
                )
            }
        }

        return ScheduleRule(
            recurrence = recurrence,
            timing = timing,
            window = RecurrenceWindow(
                startDate = LocalDate.parse(rule.startDate),
                endDateInclusive = rule.endDateInclusive?.let(LocalDate::parse)
            ),
            isEnabled = rule.isEnabled
        )
    }

    fun toRuleEntity(
        ownerType: ScheduleOwnerType,
        ownerId: Long,
        rule: ScheduleRule
    ): ScheduleRuleEntity {
        val recurrenceType = when (rule.recurrence) {
            is RecurrencePattern.Daily -> ScheduleRecurrenceType.DAILY
            is RecurrencePattern.Weekly -> ScheduleRecurrenceType.WEEKLY
        }

        val intervalValue = when (val recurrence = rule.recurrence) {
            is RecurrencePattern.Daily -> recurrence.intervalDays
            is RecurrencePattern.Weekly -> recurrence.intervalWeeks
        }

        val timingType = when (rule.timing) {
            is ScheduleTiming.FixedTimes -> ScheduleTimingType.FIXED
            is ScheduleTiming.AnchoredTimes -> ScheduleTimingType.ANCHORED
        }

        return ScheduleRuleEntity(
            ownerType = ownerType,
            ownerId = ownerId,
            recurrenceType = recurrenceType,
            intervalValue = intervalValue,
            startDate = rule.window.startDate.toString(),
            endDateInclusive = rule.window.endDateInclusive?.toString(),
            isEnabled = rule.isEnabled,
            timingType = timingType
        )
    }

    fun toWeeklyDayEntities(
        scheduleRuleId: Long,
        rule: ScheduleRule
    ): List<ScheduleRuleWeeklyDayEntity> {
        val recurrence = rule.recurrence as? RecurrencePattern.Weekly ?: return emptyList()

        return recurrence.daysOfWeek
            .sortedBy { it.isoDayNumber }
            .map { dayOfWeek ->
                ScheduleRuleWeeklyDayEntity(
                    scheduleRuleId = scheduleRuleId,
                    dayOfWeek = dayOfWeek.name
                )
            }
    }

    fun toFixedTimeEntities(
        scheduleRuleId: Long,
        rule: ScheduleRule
    ): List<ScheduleRuleFixedTimeEntity> {
        val timing = rule.timing as? ScheduleTiming.FixedTimes ?: return emptyList()

        return timing.times.map { timeSpec ->
            ScheduleRuleFixedTimeEntity(
                scheduleRuleId = scheduleRuleId,
                time = timeSpec.time.toString(),
                label = timeSpec.label,
                sortOrderHint = timeSpec.sortOrderHint
            )
        }
    }

    fun toAnchoredTimeEntities(
        scheduleRuleId: Long,
        rule: ScheduleRule
    ): List<ScheduleRuleAnchoredTimeEntity> {
        val timing = rule.timing as? ScheduleTiming.AnchoredTimes ?: return emptyList()

        return timing.occurrences.map { anchoredSpec ->
            ScheduleRuleAnchoredTimeEntity(
                scheduleRuleId = scheduleRuleId,
                anchor = anchoredSpec.anchor.name,
                offsetMinutes = anchoredSpec.offsetMinutes,
                label = anchoredSpec.label,
                sortOrderHint = anchoredSpec.sortOrderHint
            )
        }
    }
}