package com.example.hastanghubaga.domain.schedule.timing

import com.example.hastanghubaga.domain.schedule.model.ResolvedScheduleTime
import kotlinx.datetime.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Test

class SortResolvedScheduleTimesUseCaseTest {

    private val useCase = SortResolvedScheduleTimesUseCase()

    @Test
    fun empty_list_returns_empty() {
        val result = useCase(emptyList())

        assertEquals(emptyList<ResolvedScheduleTime>(), result)
    }

    @Test
    fun sorts_by_time_first() {
        val input = listOf(
            time(10, 0),
            time(8, 0),
            time(9, 0)
        )

        val result = useCase(input)

        assertEquals(
            listOf(
                time(8, 0),
                time(9, 0),
                time(10, 0)
            ),
            result
        )
    }

    @Test
    fun sorts_by_sortOrderHint_when_times_equal() {
        val input = listOf(
            time(8, 0, sortOrderHint = 2),
            time(8, 0, sortOrderHint = 1),
            time(8, 0, sortOrderHint = null)
        )

        val result = useCase(input)

        assertEquals(
            listOf(
                time(8, 0, sortOrderHint = 1),
                time(8, 0, sortOrderHint = 2),
                time(8, 0, sortOrderHint = null)
            ),
            result
        )
    }

    @Test
    fun sorts_by_label_when_time_and_sort_order_equal() {
        val input = listOf(
            time(8, 0, label = "B"),
            time(8, 0, label = "A"),
            time(8, 0, label = null)
        )

        val result = useCase(input)

        assertEquals(
            listOf(
                time(8, 0, label = "A"),
                time(8, 0, label = "B"),
                time(8, 0, label = null)
            ),
            result
        )
    }

    @Test
    fun full_sort_order_time_then_hint_then_label() {
        val input = listOf(
            time(9, 0, sortOrderHint = 1, label = "C"),
            time(8, 0, sortOrderHint = 2, label = "B"),
            time(8, 0, sortOrderHint = 1, label = "A"),
            time(8, 0, sortOrderHint = 1, label = "B")
        )

        val result = useCase(input)

        assertEquals(
            listOf(
                time(8, 0, sortOrderHint = 1, label = "A"),
                time(8, 0, sortOrderHint = 1, label = "B"),
                time(8, 0, sortOrderHint = 2, label = "B"),
                time(9, 0, sortOrderHint = 1, label = "C")
            ),
            result
        )
    }

    private fun time(
        hour: Int,
        minute: Int,
        label: String? = null,
        sortOrderHint: Int? = null
    ): ResolvedScheduleTime {
        return ResolvedScheduleTime(
            time = LocalTime(hour, minute),
            label = label,
            sortOrderHint = sortOrderHint
        )
    }
}