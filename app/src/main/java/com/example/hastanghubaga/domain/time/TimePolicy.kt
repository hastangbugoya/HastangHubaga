package com.example.hastanghubaga.domain.time

import java.time.*

/**
 * Centralized policy for all date & time handling in the app.
 *
 * ## Core Rules
 * - Absolute moments are stored as UTC epoch millis
 * - User-facing dates are interpreted in the device's local timezone
 * - Clock-only values (e.g. default event times) are seconds-from-midnight
 * - Sentinel anchors (e.g. MIDNIGHT) are conceptual, not stored as timestamps
 *
 * This object exists to prevent silent timezone drift and inconsistent
 * date filtering across the app.
 */
object TimePolicy {

    private var zoneOverride: ZoneId? = null
    /** Device local timezone (single source of truth) */
    val localZoneId: ZoneId
        get() = zoneOverride ?: ZoneId.systemDefault()

    fun <T> withZone(zoneId: ZoneId, block: () -> T): T {
        val previous = zoneOverride
        zoneOverride = zoneId
        return try {
            block()
        } finally {
            zoneOverride = previous
        }
    }

    /** Clock used for all "now" calls (injectable later if needed) */
    val clock: Clock = Clock.systemUTC()

    // -------------------------------------------------------------------------
    // NOW / TODAY
    // -------------------------------------------------------------------------

    /** Current moment as UTC epoch millis */
    fun nowUtcMillis(): Long =
        Instant.now(clock).toEpochMilli()

    /** Today's date in the user's local timezone */
    fun todayLocal(): LocalDate =
        LocalDate.now(localZoneId)

    // -------------------------------------------------------------------------
    // LOCAL DATE → UTC MILLIS
    // -------------------------------------------------------------------------

    /**
     * Converts a local date + time into UTC epoch millis.
     *
     * Use this for:
     * - Activities
     * - Meals
     * - Any timeline event stored as an absolute moment
     */
    fun localDateTimeToUtcMillis(
        date: LocalDate,
        time: LocalTime
    ): Long =
        LocalDateTime.of(date, time)
            .atZone(localZoneId)
            .toInstant()
            .toEpochMilli()

    /**
     * Converts a LocalDateTime (assumed local) into UTC epoch millis.
     */
    fun localDateTimeToUtcMillis(
        dateTime: LocalDateTime
    ): Long =
        dateTime
            .atZone(localZoneId)
            .toInstant()
            .toEpochMilli()

    // -------------------------------------------------------------------------
    // UTC MILLIS → LOCAL
    // -------------------------------------------------------------------------

    /**
     * Converts stored UTC epoch millis into a local LocalDateTime.
     *
     * Use this when reading from the database.
     */
    fun utcMillisToLocalDateTime(
        millis: Long
    ): LocalDateTime =
        Instant.ofEpochMilli(millis)
            .atZone(localZoneId)
            .toLocalDateTime()

    /**
     * Extracts the local date from a UTC epoch millis timestamp.
     *
     * Use this for date filtering.
     */
    fun utcMillisToLocalDate(
        millis: Long
    ): LocalDate =
        Instant.ofEpochMilli(millis)
            .atZone(localZoneId)
            .toLocalDate()

    // -------------------------------------------------------------------------
    // CLOCK-ONLY VALUES (DEFAULT EVENT TIMES)
    // -------------------------------------------------------------------------

    /**
     * Resolves a "seconds-from-midnight" clock value into a LocalDateTime.
     *
     * This is intentionally timezone-agnostic.
     */
    fun resolveClockSeconds(
        date: LocalDate,
        secondsFromMidnight: Int
    ): LocalDateTime =
        date.atStartOfDay()
            .plusSeconds(secondsFromMidnight.toLong())

    // -------------------------------------------------------------------------
    // RANGE HELPERS
    // -------------------------------------------------------------------------

    /**
     * Returns the UTC millis range that fully covers a local date.
     *
     * Useful for Room queries.
     */
    fun utcRangeForLocalDate(
        date: LocalDate
    ): Pair<Long, Long> {
        val start = date
            .atStartOfDay(localZoneId)
            .toInstant()
            .toEpochMilli()

        val end = date
            .plusDays(1)
            .atStartOfDay(localZoneId)
            .toInstant()
            .toEpochMilli() - 1

        return start to end
    }
}
