package com.example.hastanghubaga.domain.schedule.timing

import com.example.hastanghubaga.domain.schedule.model.ResolvedScheduleTime

class SortResolvedScheduleTimesUseCase {

    operator fun invoke(
        items: List<ResolvedScheduleTime>
    ): List<ResolvedScheduleTime> {
        return items.sortedWith(
            compareBy<ResolvedScheduleTime>(
                { it.time },
                { it.sortOrderHint ?: Int.MAX_VALUE },
                { it.label == null },
                { it.label ?: "" }
            )
        )
    }
}