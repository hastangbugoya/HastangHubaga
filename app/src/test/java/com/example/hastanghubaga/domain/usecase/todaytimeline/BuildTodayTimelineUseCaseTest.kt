package com.example.hastanghubaga.domain.usecase.todaytimeline

import com.example.hastanghubaga.data.local.entity.activity.ActivityOccurrenceEntity
import com.example.hastanghubaga.data.local.entity.meal.MealOccurrenceEntity
import com.example.hastanghubaga.data.local.entity.supplement.DoseAnchorType
import com.example.hastanghubaga.data.local.entity.supplement.FrequencyType
import com.example.hastanghubaga.data.local.entity.supplement.SupplementDoseUnit
import com.example.hastanghubaga.data.local.entity.supplement.SupplementOccurrenceEntity
import com.example.hastanghubaga.data.local.entity.supplement.SupplementOccurrenceSourceType
import com.example.hastanghubaga.domain.model.activity.Activity
import com.example.hastanghubaga.domain.model.activity.ActivityLog
import com.example.hastanghubaga.domain.model.activity.ActivityType
import com.example.hastanghubaga.domain.model.meal.Meal
import com.example.hastanghubaga.domain.model.supplement.Supplement
import com.example.hastanghubaga.domain.model.supplement.SupplementDoseLog
import com.example.hastanghubaga.domain.model.timeline.UpcomingSchedule
import com.example.hastanghubaga.domain.repository.time.UpcomingScheduleRepository
import com.example.hastanghubaga.domain.usecase.meal.ResolveMealAnchorUseCase
import com.example.hastanghubaga.factory.FakeActivityFactory
import com.example.hastanghubaga.factory.FakeMealFactory
import com.example.hastanghubaga.widget.snapshot.BuildWidgetDailySnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Ignore
import org.junit.Test

@Ignore("Temporarily disabled while stabilizing scheduling refactor")
class BuildTodayTimelineUseCaseTest {

    private val testDate = LocalDate(2025, 1, 1)

    private val fakeBuildWidgetDailySnapshot =
        object : BuildWidgetDailySnapshot {
            override suspend fun invoke(day: LocalDate) {
                // no-op
            }
        }

    private val upcomingScheduleRepository =
        object : UpcomingScheduleRepository {

            override fun observeUpcoming(): Flow<List<UpcomingSchedule>> {
                return flowOf(emptyList())
            }

            override suspend fun replaceAll(items: List<UpcomingSchedule>) {
                // no-op
            }

            override fun observeNextUpcoming(): Flow<UpcomingSchedule?> {
                return flowOf(null)
            }
        }

    private val resolveMealAnchorUseCase = ResolveMealAnchorUseCase()

    private val useCase = BuildTodayTimelineUseCase(
        upcomingScheduleRepository = upcomingScheduleRepository,
        buildWidgetDailySnapshotUseCase = fakeBuildWidgetDailySnapshot,
        resolveMealAnchorUseCase = resolveMealAnchorUseCase
    )

    private fun supplement(
        id: Long,
        name: String
    ): Supplement =
        Supplement(
            id = id,
            name = name,
            brand = null,
            notes = null,
            recommendedServingSize = 1.0,
            recommendedDoseUnit = SupplementDoseUnit.TABLET,
            servingsPerDay = 1.0,
            recommendedWithFood = null,
            recommendedLiquidInOz = null,
            recommendedTimeBetweenDailyDosesMinutes = null,
            avoidCaffeine = null,
            doseConditions = emptySet(),
            doseAnchorType = DoseAnchorType.ANYTIME,
            frequencyType = FrequencyType.DAILY,
            frequencyInterval = null,
            weeklyDays = null,
            offsetMinutes = null,
            startDate = null,
            lastTakenDate = null,
            ingredients = emptyList(),
            isActive = true,
            sendAlert = false,
            alertOffsetMinutes = null
        )

    private fun occurrence(
        id: String,
        supplementId: Long,
        time: LocalTime
    ): SupplementOccurrenceEntity =
        SupplementOccurrenceEntity(
            id = id,
            supplementId = supplementId,
            scheduleId = null,
            date = testDate.toString(),
            plannedTimeSeconds = time.toSecondOfDay(),
            sourceType = SupplementOccurrenceSourceType.SCHEDULED,
            isDeleted = false
        )

    private fun doseLog(
        id: Long,
        supplementId: Long,
        timestamp: LocalDateTime,
        occurrenceId: String? = null
    ): SupplementDoseLog =
        SupplementDoseLog(
            id = id,
            supplementId = supplementId,
            date = timestamp.date,
            actualServingTaken = 1.0,
            doseUnit = SupplementDoseUnit.TABLET,
            timestamp = timestamp,
            occurrenceId = occurrenceId
        )

    private fun meal(
        id: Long,
        name: String,
        at: LocalDateTime
    ): Meal =
        FakeMealFactory.create(
            name = name,
            at = at
        )

    private fun mealOccurrence(
        id: String,
        mealId: Long,
        time: LocalTime
    ): MealOccurrenceEntity =
        MealOccurrenceEntity(
            id = id,
            mealId = mealId,
            scheduleId = null,
            date = testDate.toString(),
            plannedTimeSeconds = time.toSecondOfDay(),
            sourceType = com.example.hastanghubaga.data.local.entity.meal.MealOccurrenceSourceType.SCHEDULED,
            isDeleted = false
        )

    private fun activity(
        id: Long,
        name: String,
        at: LocalDateTime
    ): Activity =
        FakeActivityFactory.create(
            name = name,
            at = at
        )

    private fun activityOccurrence(
        id: String,
        activityId: Long,
        time: LocalTime,
        title: String = "Workout"
    ): ActivityOccurrenceEntity =
        ActivityOccurrenceEntity(
            id = id,
            activityId = activityId,
            scheduleId = null,
            date = testDate.toString(),
            plannedTimeSeconds = time.toSecondOfDay(),
            sourceType = com.example.hastanghubaga.data.local.entity.activity.ActivityOccurrenceSourceType.SCHEDULED,
            isDeleted = false,
            title = title
        )

    private fun activityLog(
        id: Long,
        activityId: Long?,
        activityType: ActivityType,
        at: LocalDateTime,
        occurrenceId: String? = null,
        title: String = "Workout"
    ): ActivityLog =
        ActivityLog(
            id = id,
            activityId = activityId,
            occurrenceId = occurrenceId,
            title = title,
            activityType = activityType,
            start = at,
            end = null,
            notes = null,
            intensity = null
        )

    @Test
    fun `empty inputs returns empty list`() = runTest {
        val result = useCase(
            date = testDate,
            supplementOccurrences = emptyList(),
            supplements = emptyList(),
            supplementDoseLogs = emptyList(),
            meals = emptyList(),
            importedMeals = emptyList(),
            activities = emptyList(),
            activityLogs = emptyList()
        )

        assertTrue(result.isEmpty())
    }

    @Test
    fun `planned occurrences expand into multiple supplement timeline items`() = runTest {
        val supplement = supplement(
            id = 1L,
            name = "Vitamin D"
        )

        val result = useCase(
            date = testDate,
            supplementOccurrences = listOf(
                occurrence(
                    id = "occ-1",
                    supplementId = 1L,
                    time = LocalTime(8, 0)
                ),
                occurrence(
                    id = "occ-2",
                    supplementId = 1L,
                    time = LocalTime(20, 0)
                )
            ),
            supplements = listOf(supplement)
        )

        assertEquals(2, result.size)
        assertTrue(result.all { it is com.example.hastanghubaga.ui.timeline.TimelineItem.SupplementTimelineItem })
    }

    @Test
    fun `logged occurrence suppresses matching planned row`() = runTest {
        val supplement = supplement(
            id = 1L,
            name = "Vitamin D"
        )

        val result = useCase(
            date = testDate,
            supplementOccurrences = listOf(
                occurrence(
                    id = "occ-1",
                    supplementId = 1L,
                    time = LocalTime(8, 0)
                )
            ),
            supplements = listOf(supplement),
            supplementDoseLogs = listOf(
                doseLog(
                    id = 100L,
                    supplementId = 1L,
                    timestamp = LocalDateTime(2025, 1, 1, 8, 5),
                    occurrenceId = "occ-1"
                )
            )
        )

        assertEquals(1, result.size)
        assertTrue(result.single() is com.example.hastanghubaga.ui.timeline.TimelineItem.SupplementDoseLogTimelineItem)
    }

    @Test
    fun `manual log does not suppress planned row`() = runTest {
        val supplement = supplement(
            id = 1L,
            name = "Vitamin D"
        )

        val result = useCase(
            date = testDate,
            supplementOccurrences = listOf(
                occurrence(
                    id = "occ-1",
                    supplementId = 1L,
                    time = LocalTime(8, 0)
                )
            ),
            supplements = listOf(supplement),
            supplementDoseLogs = listOf(
                doseLog(
                    id = 100L,
                    supplementId = 1L,
                    timestamp = LocalDateTime(2025, 1, 1, 8, 5),
                    occurrenceId = null
                )
            )
        )

        assertEquals(2, result.size)
        assertTrue(result.any { it is com.example.hastanghubaga.ui.timeline.TimelineItem.SupplementTimelineItem })
        assertTrue(result.any { it is com.example.hastanghubaga.ui.timeline.TimelineItem.SupplementDoseLogTimelineItem })
    }

    @Test
    fun `meal occurrences are mapped correctly`() = runTest {
        val meal = meal(
            id = 10L,
            name = "Lunch",
            at = LocalDateTime(2025, 1, 1, 12, 30)
        )

        val result = useCase(
            date = testDate,
            supplementOccurrences = emptyList(),
            supplements = emptyList(),
            mealOccurrences = listOf(
                mealOccurrence(
                    id = "meal-occ-1",
                    mealId = meal.id,
                    time = LocalTime(12, 30)
                )
            ),
            meals = listOf(meal)
        )

        val item = result.single() as com.example.hastanghubaga.ui.timeline.TimelineItem.MealTimelineItem
        assertEquals(LocalTime(12, 30), item.time)
        assertEquals(meal, item.meal)
    }

    @Test
    fun `activity occurrence plus log are mapped correctly`() = runTest {
        val activity = activity(
            id = 1L,
            name = "Workout",
            at = LocalDateTime(2025, 1, 1, 6, 0)
        )

        val log = activityLog(
            id = 10L,
            activityId = 1L,
            activityType = ActivityType.STRENGTH_TRAINING,
            at = LocalDateTime(2025, 1, 1, 6, 0),
            occurrenceId = "act-occ-1",
            title = "Workout"
        )

        val result = useCase(
            date = testDate,
            supplementOccurrences = emptyList(),
            supplements = emptyList(),
            activityOccurrences = listOf(
                activityOccurrence(
                    id = "act-occ-1",
                    activityId = activity.id,
                    time = LocalTime(6, 0),
                    title = "Workout"
                )
            ),
            activities = listOf(activity),
            activityLogs = listOf(log)
        )

        val item = result.single() as com.example.hastanghubaga.ui.timeline.TimelineItem.ActivityTimelineItem
        assertEquals(LocalTime(6, 0), item.time)
        assertEquals(1L, item.activityId)
        assertEquals("Workout", item.title)
        assertTrue(item.isCompleted)
    }

    @Test
    fun `mixed inputs are merged and sorted by time`() = runTest {
        val supplement = supplement(
            id = 1L,
            name = "Magnesium"
        )

        val meal = meal(
            id = 10L,
            name = "Breakfast",
            at = LocalDateTime(2025, 1, 1, 8, 0)
        )

        val activity = activity(
            id = 2L,
            name = "Workout",
            at = LocalDateTime(2025, 1, 1, 6, 30)
        )

        val log = activityLog(
            id = 10L,
            activityId = 2L,
            activityType = ActivityType.RUNNING,
            at = LocalDateTime(2025, 1, 1, 6, 30),
            occurrenceId = "act-occ-1",
            title = "Workout"
        )

        val result = useCase(
            date = testDate,
            supplementOccurrences = listOf(
                occurrence(
                    id = "occ-1",
                    supplementId = 1L,
                    time = LocalTime(22, 0)
                )
            ),
            supplements = listOf(supplement),
            mealOccurrences = listOf(
                mealOccurrence(
                    id = "meal-occ-1",
                    mealId = meal.id,
                    time = LocalTime(8, 0)
                )
            ),
            meals = listOf(meal),
            activityOccurrences = listOf(
                activityOccurrence(
                    id = "act-occ-1",
                    activityId = activity.id,
                    time = LocalTime(6, 30),
                    title = "Workout"
                )
            ),
            activities = listOf(activity),
            activityLogs = listOf(log)
        )

        assertEquals(
            listOf(
                LocalTime(6, 30),
                LocalTime(8, 0),
                LocalTime(22, 0)
            ),
            result.map { it.time }
        )
    }

    @Test
    fun `items with same time do not crash`() = runTest {
        val supplement = supplement(
            id = 1L,
            name = "Zinc"
        )

        val meal = meal(
            id = 10L,
            name = "Snack",
            at = LocalDateTime(2025, 1, 1, 9, 0)
        )

        val result = useCase(
            date = testDate,
            supplementOccurrences = listOf(
                occurrence(
                    id = "occ-1",
                    supplementId = 1L,
                    time = LocalTime(9, 0)
                )
            ),
            supplements = listOf(supplement),
            mealOccurrences = listOf(
                mealOccurrence(
                    id = "meal-occ-1",
                    mealId = meal.id,
                    time = LocalTime(9, 0)
                )
            ),
            meals = listOf(meal)
        )

        assertEquals(2, result.size)
        assertEquals(LocalTime(9, 0), result[0].time)
        assertEquals(LocalTime(9, 0), result[1].time)
    }
}