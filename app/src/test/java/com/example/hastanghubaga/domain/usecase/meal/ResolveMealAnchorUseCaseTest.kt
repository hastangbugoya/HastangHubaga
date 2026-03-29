package com.example.hastanghubaga.domain.usecase.meal

import com.example.hastanghubaga.data.local.entity.meal.MealType
import com.example.hastanghubaga.domain.model.meal.Meal
import com.example.hastanghubaga.domain.schedule.model.TimeAnchor
import kotlinx.datetime.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class ResolveMealAnchorUseCaseTest {

    private lateinit var useCase: ResolveMealAnchorUseCase

    @Before
    fun setup() {
        useCase = ResolveMealAnchorUseCase()
    }

    private fun meal(
        type: MealType,
        treatAsAnchor: MealType? = null
    ): Meal {
        return Meal(
            id = 1L,
            name = "Test",
            type = type,
            treatAsAnchor = treatAsAnchor,
            timestamp = LocalDateTime(2026, 1, 1, 8, 0),
            nutrition = null,
            notes = null
        )
    }

    @Test
    fun `returns BREAKFAST anchor when type is BREAKFAST`() {
        val result = useCase(meal(MealType.BREAKFAST))
        assertEquals(TimeAnchor.BREAKFAST, result)
    }

    @Test
    fun `returns LUNCH anchor when type is LUNCH`() {
        val result = useCase(meal(MealType.LUNCH))
        assertEquals(TimeAnchor.LUNCH, result)
    }

    @Test
    fun `returns DINNER anchor when type is DINNER`() {
        val result = useCase(meal(MealType.DINNER))
        assertEquals(TimeAnchor.DINNER, result)
    }

    @Test
    fun `returns null for non-anchor types`() {
        assertNull(useCase(meal(MealType.SNACK)))
        assertNull(useCase(meal(MealType.PRE_WORKOUT)))
        assertNull(useCase(meal(MealType.POST_WORKOUT)))
        assertNull(useCase(meal(MealType.CUSTOM)))
    }

    @Test
    fun `treatAsAnchor overrides type`() {
        val result = useCase(
            meal(
                type = MealType.SNACK,
                treatAsAnchor = MealType.DINNER
            )
        )
        assertEquals(TimeAnchor.DINNER, result)
    }

    @Test
    fun `treatAsAnchor still respects mapping rules`() {
        val result = useCase(
            meal(
                type = MealType.BREAKFAST,
                treatAsAnchor = MealType.SNACK // not anchor-capable
            )
        )
        assertNull(result)
    }

    @Test
    fun `treatAsAnchor takes precedence over valid type`() {
        val result = useCase(
            meal(
                type = MealType.BREAKFAST,
                treatAsAnchor = MealType.LUNCH
            )
        )
        assertEquals(TimeAnchor.LUNCH, result)
    }
}