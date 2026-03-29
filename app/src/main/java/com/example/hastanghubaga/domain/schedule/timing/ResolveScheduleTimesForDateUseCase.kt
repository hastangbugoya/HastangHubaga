package com.example.hastanghubaga.domain.schedule.timing

import com.example.hastanghubaga.domain.schedule.model.AnchoredTimeSpec
import com.example.hastanghubaga.domain.schedule.model.ResolvedScheduleTime
import com.example.hastanghubaga.domain.schedule.model.ScheduleRule
import com.example.hastanghubaga.domain.schedule.model.ScheduleTiming
import kotlinx.datetime.LocalDate

class ResolveScheduleTimesForDateUseCase(
    private val resolveAnchorTimeUseCase: ResolveAnchorTimeUseCase,
    private val applyAnchorOffsetUseCase: ApplyAnchorOffsetUseCase,
    private val sortResolvedScheduleTimesUseCase: SortResolvedScheduleTimesUseCase
) {

    operator fun invoke(
        rule: ScheduleRule,
        date: LocalDate,
        anchorContextProvider: ((LocalDate) -> com.example.hastanghubaga.domain.schedule.model.AnchorTimeContext?)? = null
    ): List<ResolvedScheduleTime> {

        return when (val timing = rule.timing) {
            is ScheduleTiming.FixedTimes -> {
                sortResolvedScheduleTimesUseCase(
                    timing.times.map {
                        ResolvedScheduleTime(
                            time = it.time,
                            label = it.label,
                            sortOrderHint = it.sortOrderHint
                        )
                    }
                )
            }

            is ScheduleTiming.AnchoredTimes -> {
                val context = anchorContextProvider?.invoke(date)
                    ?: return emptyList()

                val resolved = timing.occurrences.mapNotNull { spec ->
                    resolveAnchoredSpec(spec, context)
                }

                sortResolvedScheduleTimesUseCase(resolved)
            }
        }
    }

    private fun resolveAnchoredSpec(
        spec: AnchoredTimeSpec,
        context: com.example.hastanghubaga.domain.schedule.model.AnchorTimeContext
    ): ResolvedScheduleTime? {
        val baseTime = resolveAnchorTimeUseCase(
            anchor = spec.anchor,
            context = context
        ) ?: return null

        val adjusted = applyAnchorOffsetUseCase(
            baseTime = baseTime,
            offsetMinutes = spec.offsetMinutes
        )

        return ResolvedScheduleTime(
            time = adjusted,
            label = spec.label,
            sortOrderHint = spec.sortOrderHint
        )
    }
}