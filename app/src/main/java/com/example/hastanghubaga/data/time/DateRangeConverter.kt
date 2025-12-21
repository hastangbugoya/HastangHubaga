package com.example.hastanghubaga.data.time
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import java.time.Instant

object DateRangeConverter {

    fun utcRangeForLocalDate(
        date: LocalDate,
        timeZone: TimeZone = TimeZone.currentSystemDefault()
    ): Pair<Long, Long> {
        val startInstant = date.atStartOfDayIn(timeZone)
        val endInstant = date
            .plus(1, DateTimeUnit.DAY)
            .atStartOfDayIn(timeZone)

        return startInstant.toEpochMilliseconds() to
                endInstant.toEpochMilliseconds()
    }
}
