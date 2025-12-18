package com.example.hastanghubaga.domain.time

import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/**
 * DO NOT perform date/time math outside this class.
 * DST correctness is enforced by unit tests.
 */

class TimePolicyDstTest {
    @Test
    fun `DST start day has 23-hour UTC range`() {
        TimePolicy.withZone(ZoneId.of("America/Los_Angeles")) {
            val date = LocalDate.of(2025, 3, 9) // DST starts
            val (start, end) = TimePolicy.utcRangeForLocalDate(date)

            val hours = Duration.ofMillis(end - start + 1).toHours()
            assertEquals(23, hours)
        }
    }

    @Test
    fun `DST end day has 25-hour UTC range`() {
        TimePolicy.withZone(ZoneId.of("America/Los_Angeles")) {
            val date = LocalDate.of(2025, 11, 2)

            val (start, end) = TimePolicy.utcRangeForLocalDate(date)
            val hours = Duration.ofMillis(end - start + 1).toHours()

            assertEquals(25, hours)
        }
    }

    @Test
    fun `normal day has 24-hour UTC range`() {
        TimePolicy.withZone(ZoneId.of("America/Los_Angeles")) {
            val date = LocalDate.of(2025, 2, 10)

            val (start, end) = TimePolicy.utcRangeForLocalDate(date)
            val hours = Duration.ofMillis(end - start + 1).toHours()

            assertEquals(24, hours)
        }
    }

    @Test
    fun `nonexistent local time during DST start does not crash`() {
        TimePolicy.withZone(ZoneId.of("America/Los_Angeles")) {
            val date = LocalDate.of(2025, 3, 9)

            val millis = TimePolicy.localDateTimeToUtcMillis(
                date,
                LocalTime.of(2, 30)
            )

            val local = TimePolicy.utcMillisToLocalDateTime(millis)

            // JVM shifts forward safely to 03:30
            assertEquals(3, local.hour)
        }
    }
    @Test
    fun `ambiguous local time during DST end resolves safely`() {
        TimePolicy.withZone(ZoneId.of("America/Los_Angeles")) {
            val date = LocalDate.of(2025, 11, 2)

            val millis = TimePolicy.localDateTimeToUtcMillis(
                date,
                LocalTime.of(1, 30)
            )

            val local = TimePolicy.utcMillisToLocalDateTime(millis)

            assertEquals(1, local.hour)
            assertEquals(30, local.minute)
        }
    }

    @Test
    fun `same local time produces different UTC millis in different zones`() {
        val date = LocalDate.of(2025, 1, 1)
        val time = LocalTime.of(8, 0)

        val pstMillis = TimePolicy.withZone(ZoneId.of("America/Los_Angeles")) {
            TimePolicy.localDateTimeToUtcMillis(date, time)
        }

        val utcMillis = TimePolicy.withZone(ZoneId.of("UTC")) {
            TimePolicy.localDateTimeToUtcMillis(date, time)
        }

        assertNotEquals(pstMillis, utcMillis)
    }

    @Test
    fun `item scheduled at 23_59 stays within same local day`() {
        TimePolicy.withZone(ZoneId.of("America/Los_Angeles")) {
            val date = LocalDate.of(2025, 3, 9)

            val millis = TimePolicy.localDateTimeToUtcMillis(
                date,
                LocalTime.of(23, 59)
            )

            val local = TimePolicy.utcMillisToLocalDateTime(millis)

            assertEquals(date, local.toLocalDate())
        }
    }

    @Test
    fun `UTC zone always has 24-hour days`() {
        TimePolicy.withZone(ZoneId.of("UTC")) {
            val date = LocalDate.of(2025, 6, 15)

            val (start, end) = TimePolicy.utcRangeForLocalDate(date)
            val hours = Duration.ofMillis(end - start + 1).toHours()

            assertEquals(24, hours)
        }
    }


    @Test
    fun `UTC invariant always 24 hours`() {
        TimePolicy.withZone(ZoneId.of("UTC")) {
            listOf(
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 3, 9),  // DST elsewhere
                LocalDate.of(2025, 11, 2)  // DST elsewhere
            ).forEach { date ->
                val (start, end) = TimePolicy.utcRangeForLocalDate(date)
                val hours = Duration.ofMillis(end - start + 1).toHours()

                assertEquals(24, hours)
            }
        }
    }

    @Test
    fun `UTC invariants must never change`() {
        val date = LocalDate.of(2025, 1, 1)
        val (start, end) = TimePolicy.utcRangeForLocalDate(date)

        val hours = Duration.ofMillis(end - start + 1).toHours()
        assertEquals(24, hours)
    }


}




