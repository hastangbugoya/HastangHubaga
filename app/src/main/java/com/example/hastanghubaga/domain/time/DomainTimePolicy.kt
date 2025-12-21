package com.example.hastanghubaga.domain.time

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
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
     *
     * Design rules:
     * - Uses ONLY kotlinx.datetime
     * - Does NOT depend on Android, Room, AlarmManager, or java.time
     * - Safe to use in unit tests
     *
     * This object answers the question:
     * "What date and time does the user mean?"
     *
     * val (date, time) =
     *     DomainTimePolicy.resolveIntent(
     *         intent = input.timeUseIntent,
     *         clock = clock
     *     )
     *
     * repository.logDose(
     *     supplementId = input.supplementId,
     *     date = date,
     *     time = time,
     *     fractionTaken = input.fractionTaken,
     *     doseUnit = input.unit
     * )
     *
     * Example A: Logging a supplement dose (Room / DB)
     * override suspend fun logDose(
     *     supplementId: Long,
     *     date: kotlinx.datetime.LocalDate,
     *     time: kotlinx.datetime.LocalTime,
     *     fractionTaken: Double,
     *     doseUnit: SupplementDoseUnit
     * ) {
     *     dao.insertDose(
     *         supplementId = supplementId,
     *         date = JavaTimeAdapter.toJavaLocalDate(date),
     *         time = JavaTimeAdapter.toJavaLocalTime(time),
     *         fractionTaken = fractionTaken,
     *         doseUnit = doseUnit
     *     )
     * }
     *
     * Example B: Scheduling an alarm (Android service)
     * val nextDoseInstant: kotlinx.datetime.Instant =
     *     computeNextDoseInstant()
     *
     * val triggerAt: ZonedDateTime =
     *     JavaTimeAdapter.toZonedDateTime(nextDoseInstant)
     *
     * scheduleAlarm(
     *     context = context,
     *     supplement = supplement,
     *     dateTime = triggerAt
     * )
     *
     */

    /**
     * Local time zone used for all domain-level decisions.
     *
     * Must be kotlinx.datetime.TimeZone (NOT java.time.ZoneId).
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
}
