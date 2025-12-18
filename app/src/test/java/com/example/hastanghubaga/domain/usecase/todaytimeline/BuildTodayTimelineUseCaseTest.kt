package com.example.hastanghubaga.domain.usecase.todaytimeline

import com.example.hastanghubaga.domain.model.activity.Activity
import com.example.hastanghubaga.domain.model.meal.Meal
import com.example.hastanghubaga.domain.model.supplement.SupplementWithUserSettings
import com.example.hastanghubaga.domain.model.timeline.TimelineItem
import com.example.hastanghubaga.factory.FakeActivityFactory
import com.example.hastanghubaga.factory.FakeMealFactory
import com.example.hastanghubaga.factory.FakeSupplementFactory
import com.example.hastanghubaga.factory.FakeSupplementWithUserSettingsFactory
import org.junit.Assert
import org.junit.Test
import java.time.LocalDateTime
import java.time.LocalTime

class BuildTodayTimelineUseCaseTest {

    private val useCase = BuildTodayTimelineUseCase()

    // ---------- helpers ----------

    private fun supplement(
        name: String,
        times: List<LocalTime>
    ): SupplementWithUserSettings =
        FakeSupplementWithUserSettingsFactory.create(
            name = name,
            scheduledTimes = times
        )

    private fun meal(
        name: String,
        at: LocalDateTime
    ): Meal =
        FakeMealFactory.create(
            name = name,
            at = at
        )


    private fun activity(
        name: String,
        at: LocalDateTime
    ): Activity =
        FakeActivityFactory.create(
            name = name,
            at = at
        )

    // ---------- tests ----------

    @Test
    fun `empty inputs returns empty list`() {
        val result = useCase(
            supplements = emptyList(),
            meals = emptyList(),
            activities = emptyList()
        )

        Assert.assertTrue(result.isEmpty())
    }

    @Test
    fun `supplements expand into multiple timeline items`() {
        val supplement = supplement(
            name = "Vitamin D",
            times = listOf(
                LocalTime.of(8, 0),
                LocalTime.of(20, 0)
            )
        )

        val result = useCase(
            supplements = listOf(supplement)
        )

        Assert.assertEquals(2, result.size)
        Assert.assertTrue(result.all { it is TimelineItem.SupplementTimelineItem })
    }

    @Test
    fun `meals are mapped correctly`() {
        val meal = meal(
            name = "Lunch",
            at = LocalDateTime.of(2025, 1, 1, 12, 30)
        )

        val result = useCase(
            supplements = emptyList(),
            meals = listOf(meal)
        )

        val item = result.single() as TimelineItem.MealTimelineItem
        Assert.assertEquals(LocalTime.of(12, 30), item.time)
        Assert.assertEquals(meal, item.meal)
    }

    @Test
    fun `activities are mapped correctly`() {
        val activity = activity(
            name = "Workout",
            at = LocalDateTime.of(2025, 1, 1, 6, 0)
        )

        val result = useCase(
            supplements = emptyList(),
            activities = listOf(activity)
        )

        val item = result.single() as TimelineItem.ActivityTimelineItem
        Assert.assertEquals(LocalTime.of(6, 0), item.time)
        Assert.assertEquals(activity, item.activity)
    }

    @Test
    fun `mixed inputs are merged and sorted by time`() {
        val supplement = supplement(
            "Magnesium",
            listOf(LocalTime.of(22, 0))
        )

        val meal = meal(
            "Breakfast",
            LocalDateTime.of(2025, 1, 1, 8, 0)
        )

        val activity = activity(
            "Run",
            LocalDateTime.of(2025, 1, 1, 6, 30)
        )

        val result = useCase(
            supplements = listOf(supplement),
            meals = listOf(meal),
            activities = listOf(activity)
        )

        Assert.assertEquals(
            listOf(
                LocalTime.of(6, 30),
                LocalTime.of(8, 0),
                LocalTime.of(22, 0)
            ),
            result.map { it.time }
        )
    }

    @Test
    fun `items with same time do not crash`() {
        val supplement = supplement(
            "Zinc",
            listOf(LocalTime.of(9, 0))
        )

        val meal = meal(
            "Snack",
            LocalDateTime.of(2025, 1, 1, 9, 0)
        )

        val result = useCase(
            supplements = listOf(supplement),
            meals = listOf(meal)
        )

        Assert.assertEquals(2, result.size)
        Assert.assertEquals(LocalTime.of(9, 0), result[0].time)
        Assert.assertEquals(LocalTime.of(9, 0), result[1].time)
    }
}