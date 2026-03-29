package com.example.hastanghubaga.domain.schedule.occurrence

import com.example.hastanghubaga.domain.schedule.model.AnchorTimeContext
import com.example.hastanghubaga.domain.schedule.model.ScheduleOccurrence
import com.example.hastanghubaga.domain.schedule.model.ScheduleRule
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.plus

class GetNextOccurrenceUseCase(
    private val generateOccurrencesForDateUseCase: GenerateOccurrencesForDateUseCase
) {

    operator fun invoke(
        rule: ScheduleRule,
        referenceDate: LocalDate,
        referenceTime: LocalTime? = null,
        anchorContextProvider: ((LocalDate) -> AnchorTimeContext?)? = null,
        searchHorizonDays: Int = 366
    ): ScheduleOccurrence? {

        if (!rule.isEnabled) return null

        val hardStopDate = buildHardStopDate(
            referenceDate = referenceDate,
            endDateInclusive = rule.window.endDateInclusive,
            searchHorizonDays = searchHorizonDays
        )

        var cursor = referenceDate

        while (cursor <= hardStopDate) {

            val occurrences = generateOccurrencesForDateUseCase(
                rule = rule,
                date = cursor,
                anchorContextProvider = anchorContextProvider
            )

            val filtered = if (cursor == referenceDate && referenceTime != null) {
                occurrences.filter { it.time >= referenceTime }
            } else {
                occurrences
            }

            if (filtered.isNotEmpty()) {
                return filtered.minByOrNull { it.time }
            }

            cursor = cursor.plus(DatePeriod(days = 1))
        }

        return null
    }

    private fun buildHardStopDate(
        referenceDate: LocalDate,
        endDateInclusive: LocalDate?,
        searchHorizonDays: Int
    ): LocalDate {
        val horizonStop = referenceDate.plus(DatePeriod(days = searchHorizonDays))

        return if (endDateInclusive != null && endDateInclusive < horizonStop) {
            endDateInclusive
        } else {
            horizonStop
        }
    }
}