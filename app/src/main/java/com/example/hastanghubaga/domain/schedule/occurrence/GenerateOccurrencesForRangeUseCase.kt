package com.example.hastanghubaga.domain.schedule.occurrence

import com.example.hastanghubaga.domain.schedule.model.AnchorTimeContext
import com.example.hastanghubaga.domain.schedule.model.ScheduleOccurrence
import com.example.hastanghubaga.domain.schedule.model.ScheduleRule
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

class GenerateOccurrencesForRangeUseCase(
    private val generateOccurrencesForDateUseCase: GenerateOccurrencesForDateUseCase
) {

    operator fun invoke(
        rule: ScheduleRule,
        startDate: LocalDate,
        endDateInclusive: LocalDate,
        anchorContextProvider: ((LocalDate) -> AnchorTimeContext?)? = null
    ): List<ScheduleOccurrence> {
        if (startDate > endDateInclusive) return emptyList()

        val occurrences = mutableListOf<ScheduleOccurrence>()
        var cursor = startDate

        while (cursor <= endDateInclusive) {
            occurrences += generateOccurrencesForDateUseCase(
                rule = rule,
                date = cursor,
                anchorContextProvider = anchorContextProvider
            )
            cursor = cursor.plus(DatePeriod(days = 1))
        }

        return occurrences
    }
}