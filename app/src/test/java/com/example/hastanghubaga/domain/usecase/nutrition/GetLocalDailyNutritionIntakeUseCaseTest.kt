package com.example.hastanghubaga.domain.usecase.nutrition

import com.example.hastanghubaga.domain.model.meal.DailyNutritionSummary
import com.example.hastanghubaga.domain.repository.nutrition.NutritionAggregateRepository
import com.example.hastanghubaga.domain.time.DomainTimePolicy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetLocalDailyNutritionIntakeUseCaseTest {

    @Test
    fun `maps daily nutrition summary into canonical nutrient keys`() = runTest {
        val date = LocalDate(2026, 4, 7)

        val repository = FakeNutritionAggregateRepository(
            summaries = listOf(
                DailyNutritionSummary(
                    date = date.toString(),
                    totalProtein = 150.0,
                    totalFat = 70.0,
                    totalCarbs = 220.0,
                    totalCalories = 2100.0,
                    sodium = 1800.0,
                    cholesterol = 250.0,
                    fiber = 32.0
                )
            )
        )

        val useCase = GetLocalDailyNutritionIntakeUseCase(
            nutritionAggregateRepository = repository
        )

        val result = useCase(date)

        assertEquals(
            startOfDayEpochMillis(date),
            result.date
        )

        assertEquals(7, result.nutrients.size)
        assertEquals(2100.0, result.nutrients["CALORIES_KCAL"])
        assertEquals(150.0, result.nutrients["PROTEIN_G"])
        assertEquals(220.0, result.nutrients["CARBS_G"])
        assertEquals(70.0, result.nutrients["FAT_G"])
        assertEquals(1800.0, result.nutrients["SODIUM_MG"])
        assertEquals(250.0, result.nutrients["CHOLESTEROL_MG"])
        assertEquals(32.0, result.nutrients["FIBER_G"])
    }

    @Test
    fun `returns empty nutrient map when repository has no summary for date`() = runTest {
        val date = LocalDate(2026, 4, 7)

        val repository = FakeNutritionAggregateRepository(
            summaries = emptyList()
        )

        val useCase = GetLocalDailyNutritionIntakeUseCase(
            nutritionAggregateRepository = repository
        )

        val result = useCase(date)

        assertEquals(
            startOfDayEpochMillis(date),
            result.date
        )
        assertTrue(result.nutrients.isEmpty())
    }

    @Test
    fun `omits nullable nutrients when summary fields are null`() = runTest {
        val date = LocalDate(2026, 4, 7)

        val repository = FakeNutritionAggregateRepository(
            summaries = listOf(
                DailyNutritionSummary(
                    date = date.toString(),
                    totalProtein = 100.0,
                    totalFat = 50.0,
                    totalCarbs = 175.0,
                    totalCalories = null,
                    sodium = null,
                    cholesterol = null,
                    fiber = null
                )
            )
        )

        val useCase = GetLocalDailyNutritionIntakeUseCase(
            nutritionAggregateRepository = repository
        )

        val result = useCase(date)

        assertEquals(3, result.nutrients.size)
        assertEquals(100.0, result.nutrients["PROTEIN_G"])
        assertEquals(175.0, result.nutrients["CARBS_G"])
        assertEquals(50.0, result.nutrients["FAT_G"])
        assertTrue("CALORIES_KCAL should be omitted when null", "CALORIES_KCAL" !in result.nutrients)
        assertTrue("SODIUM_MG should be omitted when null", "SODIUM_MG" !in result.nutrients)
        assertTrue("CHOLESTEROL_MG should be omitted when null", "CHOLESTEROL_MG" !in result.nutrients)
        assertTrue("FIBER_G should be omitted when null", "FIBER_G" !in result.nutrients)
    }

    @Test
    fun `ignores summaries for other dates`() = runTest {
        val date = LocalDate(2026, 4, 7)

        val repository = FakeNutritionAggregateRepository(
            summaries = listOf(
                DailyNutritionSummary(
                    date = "2026-04-06",
                    totalProtein = 90.0,
                    totalFat = 40.0,
                    totalCarbs = 160.0,
                    totalCalories = 1600.0,
                    sodium = 1200.0,
                    cholesterol = 100.0,
                    fiber = 20.0
                ),
                DailyNutritionSummary(
                    date = "2026-04-08",
                    totalProtein = 110.0,
                    totalFat = 60.0,
                    totalCarbs = 190.0,
                    totalCalories = 1900.0,
                    sodium = 1500.0,
                    cholesterol = 150.0,
                    fiber = 28.0
                )
            )
        )

        val useCase = GetLocalDailyNutritionIntakeUseCase(
            nutritionAggregateRepository = repository
        )

        val result = useCase(date)

        assertEquals(
            startOfDayEpochMillis(date),
            result.date
        )
        assertTrue(result.nutrients.isEmpty())
    }

    private class FakeNutritionAggregateRepository(
        private val summaries: List<DailyNutritionSummary>
    ) : NutritionAggregateRepository {

        override fun observeDailyNutritionSummariesInRange(
            startMillis: Long,
            endMillis: Long,
            timeZone: TimeZone
        ): Flow<List<DailyNutritionSummary>> = flowOf(summaries)
    }

    private fun startOfDayEpochMillis(date: LocalDate): Long =
        kotlinx.datetime.LocalDateTime(
            date = date,
            time = kotlinx.datetime.LocalTime(0, 0)
        )
            .toInstant(DomainTimePolicy.localTimeZone)
            .toEpochMilliseconds()
}