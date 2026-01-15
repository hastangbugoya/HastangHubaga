package com.example.hastanghubaga.data.time

import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlinx.datetime.LocalDate as KxLocalDate
import kotlinx.datetime.LocalTime as KxLocalTime
import kotlinx.datetime.Instant as KxInstant
/**
 * JavaTimeAdapter
 *
 * Explicit boundary adapter between:
 * - Domain time (kotlinx.datetime)
 * - Platform / persistence time (java.time)
 *
 * Responsibilities:
 * - Convert LocalDate / LocalTime for Room
 * - Convert Instant for alarms and scheduling
 *
 * Design rules:
 * - Conversion ONLY happens here
 * - Domain layer must never import java.time
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
object JavaTimeAdapter {

    /* -------------------- DATE -------------------- */

    fun toJavaLocalDate(
        date: KxLocalDate
    ): LocalDate =
        LocalDate.of(
            date.year,
            date.monthNumber,
            date.dayOfMonth
        )

    /* -------------------- TIME -------------------- */

    fun toJavaLocalTime(
        time: KxLocalTime
    ): LocalTime =
        LocalTime.of(
            time.hour,
            time.minute,
            time.second,
            time.nanosecond
        )

    /* -------------------- INSTANT -------------------- */

    fun toJavaInstant(
        instant: KxInstant
    ): Instant =
        Instant.ofEpochMilli(
            instant.toEpochMilliseconds()
        )

    fun toZonedDateTime(
        instant: KxInstant,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): ZonedDateTime =
        ZonedDateTime.ofInstant(
            toJavaInstant(instant),
            zoneId
        )

    /* -------------------- UTC MILLIS <-> DOMAIN -------------------- */

    fun utcMillisToDomainLocalDateTime(
        utcMillis: Long,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): kotlinx.datetime.LocalDateTime =
        KxInstant
            .fromEpochMilliseconds(utcMillis)
            .toLocalDateTime(
                kotlinx.datetime.TimeZone.of(zoneId.id)
            )

    fun domainLocalDateTimeToUtcMillis(
        localDateTime: kotlinx.datetime.LocalDateTime,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): Long =
        localDateTime
            .toInstant(
                kotlinx.datetime.TimeZone.of(zoneId.id)
            )
            .toEpochMilliseconds()


    /* -------------------- KOTLIN DATETIME -> KOTLIN LOCALTIME -------------------- */
    fun domainLocalDateTimeToLocalTime(
        localDateTime: kotlinx.datetime.LocalDateTime
    ): KxLocalTime =
        kotlinx.datetime.LocalTime(
            hour = localDateTime.hour,
            minute = localDateTime.minute,
            second = localDateTime.second,
            nanosecond = localDateTime.nanosecond
        )



}
