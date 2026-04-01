package com.example.hastanghubaga.domain.time

import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

object DomainTimePolicy {
    /**
     * DomainTimePolicy
     *
     * Single source of truth for time-related decisions in the domain layer.
     *
     * Responsibilities:
     * - Define "now" in local user time
     * - Define "today" in local user time
     * - Provide deterministic time resolution for use cases
     * - Provide consistent local-date → UTC range conversion
     *
     * Design rules:
     * - Uses ONLY kotlinx.datetime
     * - Does NOT depend on Android, Room, AlarmManager, or java.time
     * - Safe to use in unit tests
     */
    val localTimeZone: TimeZone =
        TimeZone.currentSystemDefault()

    /**
     * Returns the current local date-time using the provided clock.
     *
     * Injecting Clock makes this deterministic and testable.
     */
    fun nowLocalDateTime(
        clock: Clock
    ): LocalDateTime =
        clock.now().toLocalDateTime(localTimeZone)

    /**
     * Returns today's local date.
     *
     * This should be used instead of LocalDate.now()
     * to ensure all domain logic is timezone-consistent.
     */
    fun todayLocal(
        clock: Clock = Clock.System
    ): LocalDate =
        nowLocalDateTime(clock).date

    /**
     * Resolves a concrete (LocalDate, LocalTime) pair
     * from a TimeUseIntent.
     *
     * This is pure domain logic:
     * - No persistence
     * - No system APIs
     */
    fun resolveIntent(
        intent: TimeUseIntent,
        clock: Clock
    ): Pair<LocalDate, LocalTime> =
        when (intent) {
            TimeUseIntent.ActualNow -> {
                val now = nowLocalDateTime(clock)
                now.date to now.time
            }

            is TimeUseIntent.Scheduled ->
                todayLocal(clock) to intent.time

            is TimeUseIntent.Explicit ->
                intent.date to intent.time
        }

    /**
     * Returns the UTC epoch-millis range for a local date using [start, end) semantics.
     *
     * Contract:
     * - start = local midnight at the beginning of [date]
     * - end = local midnight at the beginning of the next local day
     *
     * This matches standard Room range queries:
     * - timestamp >= start
     * - timestamp < end
     *
     * Using a half-open interval avoids inclusive end-of-day edge cases and keeps
     * all date-scoped repositories/DAOs aligned.
     */
    fun utcMillisRangeForLocalDate(
        date: LocalDate
    ): Pair<Long, Long> {
        val start = date
            .atStartOfDayIn(localTimeZone)
            .toEpochMilliseconds()

        val end = date
            .plus(1, DateTimeUnit.DAY)
            .atStartOfDayIn(localTimeZone)
            .toEpochMilliseconds()

        return start to end
    }
}