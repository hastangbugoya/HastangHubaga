package com.example.hastanghubaga.data.local.mappers

import com.example.hastanghubaga.data.local.entity.schedule.AnchorTimeBundle
import com.example.hastanghubaga.domain.schedule.model.AnchorDateKey
import com.example.hastanghubaga.domain.schedule.model.AnchorDayKey
import com.example.hastanghubaga.domain.schedule.model.AnchorTimeContext
import com.example.hastanghubaga.domain.schedule.model.TimeAnchor
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import javax.inject.Inject

class AnchorTimeContextMapper @Inject constructor() {

    fun toDomain(
        date: LocalDate,
        bundle: AnchorTimeBundle
    ): AnchorTimeContext {
        val defaultTimes = bundle.defaultTimes.associate { entity ->
            TimeAnchor.valueOf(entity.anchor) to secondsOfDayToLocalTime(entity.timeSeconds)
        }

        val dayOfWeekOverrides = bundle.dayOfWeekOverrides.associate { entity ->
            AnchorDayKey(
                anchor = TimeAnchor.valueOf(entity.anchor),
                dayOfWeek = DayOfWeek.valueOf(entity.dayOfWeek)
            ) to secondsOfDayToLocalTime(entity.timeSeconds)
        }

        val dateOverrides = bundle.dateOverrides.associate { entity ->
            AnchorDateKey(
                anchor = TimeAnchor.valueOf(entity.anchor),
                date = LocalDate.parse(entity.date)
            ) to secondsOfDayToLocalTime(entity.timeSeconds)
        }

        return AnchorTimeContext(
            date = date,
            defaultTimes = defaultTimes,
            dayOfWeekOverrides = dayOfWeekOverrides,
            dateOverrides = dateOverrides
        )
    }

    private fun secondsOfDayToLocalTime(
        timeSeconds: Int
    ): LocalTime {
        require(timeSeconds in 0..86_399) {
            "timeSeconds must be between 0 and 86399, but was $timeSeconds"
        }

        val hour = timeSeconds / 3600
        val minute = (timeSeconds % 3600) / 60
        val second = timeSeconds % 60

        return LocalTime(
            hour = hour,
            minute = minute,
            second = second
        )
    }
}