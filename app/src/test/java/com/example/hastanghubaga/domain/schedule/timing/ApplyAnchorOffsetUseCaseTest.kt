package com.example.hastanghubaga.domain.schedule.timing

import kotlinx.datetime.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Test

class ApplyAnchorOffsetUseCaseTest {

    private val useCase = ApplyAnchorOffsetUseCase()

    @Test
    fun zero_offset_returns_same_time() {
        val base = LocalTime(8, 30)

        val result = useCase(
            baseTime = base,
            offsetMinutes = 0
        )

        assertEquals(base, result)
    }

    @Test
    fun positive_offset_within_same_day() {
        val base = LocalTime(8, 0)

        val result = useCase(
            baseTime = base,
            offsetMinutes = 30
        )

        assertEquals(LocalTime(8, 30), result)
    }

    @Test
    fun negative_offset_within_same_day() {
        val base = LocalTime(8, 30)

        val result = useCase(
            baseTime = base,
            offsetMinutes = -30
        )

        assertEquals(LocalTime(8, 0), result)
    }

    @Test
    fun positive_offset_crosses_midnight() {
        val base = LocalTime(23, 50)

        val result = useCase(
            baseTime = base,
            offsetMinutes = 20
        )

        assertEquals(LocalTime(0, 10), result)
    }

    @Test
    fun negative_offset_crosses_midnight() {
        val base = LocalTime(0, 15)

        val result = useCase(
            baseTime = base,
            offsetMinutes = -30
        )

        assertEquals(LocalTime(23, 45), result)
    }

    @Test
    fun large_positive_offset_multiple_days_wraps_correctly() {
        val base = LocalTime(1, 0)

        val result = useCase(
            baseTime = base,
            offsetMinutes = 24 * 60 + 90 // 1 day + 1h30m
        )

        assertEquals(LocalTime(2, 30), result)
    }

    @Test
    fun large_negative_offset_multiple_days_wraps_correctly() {
        val base = LocalTime(2, 0)

        val result = useCase(
            baseTime = base,
            offsetMinutes = -(24 * 60 + 30) // -1 day -30m
        )

        assertEquals(LocalTime(1, 30), result)
    }

    @Test
    fun exact_midnight_boundary_forward() {
        val base = LocalTime(23, 59)

        val result = useCase(
            baseTime = base,
            offsetMinutes = 1
        )

        assertEquals(LocalTime(0, 0), result)
    }

    @Test
    fun exact_midnight_boundary_backward() {
        val base = LocalTime(0, 0)

        val result = useCase(
            baseTime = base,
            offsetMinutes = -1
        )

        assertEquals(LocalTime(23, 59), result)
    }

    @Test
    fun full_day_offset_returns_same_time() {
        val base = LocalTime(14, 20)

        val result = useCase(
            baseTime = base,
            offsetMinutes = 24 * 60
        )

        assertEquals(base, result)
    }
}