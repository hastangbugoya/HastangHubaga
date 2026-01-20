package com.example.hastanghubaga.feature.calendar.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import java.time.YearMonth


@Composable
private fun rememberCurrentYearMonth(): YearMonth {
    val tz = remember { TimeZone.currentSystemDefault() }
    val today = remember { Clock.System.todayIn(timeZone = tz) }
    return YearMonth.of(today.year, today.month)
}