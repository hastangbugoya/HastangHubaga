package com.example.hastanghubaga.data.local.mappers

import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

private fun dayRangeUtcMillis(dateMillis: Long): Pair<Long, Long> {
    val tz = kotlinx.datetime.TimeZone.currentSystemDefault()

    val localDate = kotlinx.datetime.Instant
        .fromEpochMilliseconds(dateMillis)
        .toLocalDateTime(tz)
        .date

    val startMillis = localDate.atStartOfDayIn(tz).toEpochMilliseconds()
    val endMillis = localDate
        .plus(kotlinx.datetime.DatePeriod(days = 1))
        .atStartOfDayIn(tz)
        .toEpochMilliseconds()

    return startMillis to endMillis
}