package com.example.hastanghubaga.domain.schedule.recurrence

import kotlinx.datetime.isoDayNumber
import com.example.hastanghubaga.domain.schedule.model.RecurrencePattern
import com.example.hastanghubaga.domain.schedule.model.ScheduleRule
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.daysUntil

class IsScheduledOnDateUseCase {

    operator fun invoke(
        rule: ScheduleRule,
        date: LocalDate
    ): Boolean {
        if (!rule.isEnabled) return false
        if (date < rule.window.startDate) return false

        val endDate = rule.window.endDateInclusive
        if (endDate != null && date > endDate) return false

        return when (val recurrence = rule.recurrence) {
            is RecurrencePattern.Daily -> {
                matchesDaily(
                    startDate = rule.window.startDate,
                    date = date,
                    intervalDays = recurrence.intervalDays
                )
            }

            is RecurrencePattern.Weekly -> {
                matchesWeekly(
                    startDate = rule.window.startDate,
                    date = date,
                    intervalWeeks = recurrence.intervalWeeks,
                    daysOfWeek = recurrence.daysOfWeek
                )
            }
        }
    }

    private fun matchesDaily(
        startDate: LocalDate,
        date: LocalDate,
        intervalDays: Int
    ): Boolean {
        if (intervalDays < 1) return false

        val daysBetween = startDate.daysUntil(date)
        return daysBetween % intervalDays == 0
    }

    private fun matchesWeekly(
        startDate: LocalDate,
        date: LocalDate,
        intervalWeeks: Int,
        daysOfWeek: Set<DayOfWeek>
    ): Boolean {
        if (intervalWeeks < 1) return false
        if (daysOfWeek.isEmpty()) return false
        if (date.dayOfWeek !in daysOfWeek) return false

        val startWeekAnchor = startOfWeek(startDate)
        val dateWeekAnchor = startOfWeek(date)
        val daysBetweenAnchors = startWeekAnchor.daysUntil(dateWeekAnchor)
        val weeksBetween = daysBetweenAnchors / 7

        return weeksBetween % intervalWeeks == 0
    }

    private fun startOfWeek(
        date: LocalDate
    ): LocalDate {
        val daysFromMonday = date.dayOfWeek.isoDayNumber - DayOfWeek.MONDAY.isoDayNumber
        return date.minus(DatePeriod(days = daysFromMonday))
    }
}