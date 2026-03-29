package com.example.hastanghubaga.domain.schedule.timing

import com.example.hastanghubaga.domain.schedule.model.AnchorDateKey
import com.example.hastanghubaga.domain.schedule.model.AnchorDayKey
import com.example.hastanghubaga.domain.schedule.model.AnchorTimeContext
import com.example.hastanghubaga.domain.schedule.model.TimeAnchor
import kotlinx.datetime.LocalTime

class ResolveAnchorTimeUseCase {

    operator fun invoke(
        anchor: TimeAnchor,
        context: AnchorTimeContext
    ): LocalTime? {
        val dateOverrideKey = AnchorDateKey(
            anchor = anchor,
            date = context.date
        )
        context.dateOverrides[dateOverrideKey]?.let { return it }

        val dayOverrideKey = AnchorDayKey(
            anchor = anchor,
            dayOfWeek = context.date.dayOfWeek
        )
        context.dayOfWeekOverrides[dayOverrideKey]?.let { return it }

        return context.defaultTimes[anchor]
    }
}