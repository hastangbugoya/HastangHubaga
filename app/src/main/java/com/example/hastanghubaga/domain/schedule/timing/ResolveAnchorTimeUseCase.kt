package com.example.hastanghubaga.domain.schedule.timing

import com.example.hastanghubaga.domain.schedule.model.AnchorDateKey
import com.example.hastanghubaga.domain.schedule.model.AnchorDayKey
import com.example.hastanghubaga.domain.schedule.model.AnchorTimeContext
import com.example.hastanghubaga.domain.schedule.model.TimeAnchor
import kotlinx.datetime.LocalTime
import javax.inject.Inject

/**
 * Resolves the effective time for a [TimeAnchor] within a given [AnchorTimeContext].
 *
 * Resolution order:
 * 1. Exact date override
 * 2. Day-of-week override
 * 3. Default anchor time
 *
 * This use case is intentionally pure and reusable so meals, supplements, and
 * future anchored schedule features can share the same anchor lookup behavior.
 */
class ResolveAnchorTimeUseCase @Inject constructor() {

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