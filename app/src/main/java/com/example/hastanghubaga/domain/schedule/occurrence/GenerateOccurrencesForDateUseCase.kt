package com.example.hastanghubaga.domain.schedule.occurrence

import com.example.hastanghubaga.domain.schedule.model.ScheduleOccurrence
import com.example.hastanghubaga.domain.schedule.model.ScheduleOccurrenceKey
import com.example.hastanghubaga.domain.schedule.model.ScheduleRule
import com.example.hastanghubaga.domain.schedule.recurrence.IsScheduledOnDateUseCase
import com.example.hastanghubaga.domain.schedule.timing.ResolveScheduleTimesForDateUseCase
import kotlinx.datetime.LocalDate

class GenerateOccurrencesForDateUseCase(
    private val isScheduledOnDateUseCase: IsScheduledOnDateUseCase,
    private val resolveScheduleTimesForDateUseCase: ResolveScheduleTimesForDateUseCase
) {

    operator fun invoke(
        rule: ScheduleRule,
        date: LocalDate,
        anchorContextProvider: ((LocalDate) -> com.example.hastanghubaga.domain.schedule.model.AnchorTimeContext?)? = null
    ): List<ScheduleOccurrence> {

        if (!isScheduledOnDateUseCase(rule, date)) {
            return emptyList()
        }

        val resolvedTimes = resolveScheduleTimesForDateUseCase(
            rule = rule,
            date = date,
            anchorContextProvider = anchorContextProvider
        )

        if (resolvedTimes.isEmpty()) return emptyList()

        return resolvedTimes.map { resolved ->
            ScheduleOccurrence(
                date = date,
                time = resolved.time,
                label = resolved.label,
                key = ScheduleOccurrenceKey(
                    date = date,
                    time = resolved.time
                )
            )
        }
    }
}