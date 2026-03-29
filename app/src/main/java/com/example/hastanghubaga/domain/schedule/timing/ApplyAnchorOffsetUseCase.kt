package com.example.hastanghubaga.domain.schedule.timing

import kotlinx.datetime.LocalTime

class ApplyAnchorOffsetUseCase {

    operator fun invoke(
        baseTime: LocalTime,
        offsetMinutes: Int
    ): LocalTime {
        if (offsetMinutes == 0) return baseTime

        val totalSeconds = toSecondsOfDay(baseTime) + (offsetMinutes * 60)
        val normalized = normalizeSecondsOfDay(totalSeconds)

        return fromSecondsOfDay(normalized)
    }

    private fun toSecondsOfDay(time: LocalTime): Int {
        return (time.hour * 3600) +
                (time.minute * 60) +
                time.second
    }

    private fun normalizeSecondsOfDay(seconds: Int): Int {
        val secondsInDay = 86_400

        val mod = seconds % secondsInDay
        return if (mod >= 0) mod else mod + secondsInDay
    }

    private fun fromSecondsOfDay(seconds: Int): LocalTime {
        val hour = seconds / 3600
        val minute = (seconds % 3600) / 60
        val second = seconds % 60

        return LocalTime(
            hour = hour,
            minute = minute,
            second = second
        )
    }
}