package com.example.hastanghubaga.domain.usecase.nutrition

import com.example.hastanghubaga.domain.model.meal.DailyNutritionSummary
import com.example.hastanghubaga.domain.model.nutrition.DailyNutritionIntake
import com.example.hastanghubaga.domain.repository.nutrition.NutritionAggregateRepository
import com.example.hastanghubaga.domain.time.DomainTimePolicy
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.toInstant

/**
 * Builds a single-day HH-local nutrition intake snapshot from the existing
 * nutrition aggregate repository.
 *
 * This is the bridge between:
 * - HH local meal/supplement aggregate data
 * - the generic daily compliance engine
 *
 * Why this exists:
 * - replaces the temporary empty-intake placeholder in TodayScreenViewModel
 * - keeps compliance logic independent from storage/query details
 * - allows HH local data to drive daily compliance before AK snapshot import is wired
 *
 * Current supported nutrients:
 * - CALORIES_KCAL
 * - PROTEIN_G
 * - CARBS_G
 * - FAT_G
 * - SODIUM_MG
 * - CHOLESTEROL_MG
 * - FIBER_G
 *
 * These keys are intentionally aligned with the canonical nutrient code space
 * used by AK CsvNutrientCatalog and HH nutrition planning.
 *
 * Future AI/dev note:
 * - Keep this as a source adapter, not a rules engine
 * - When AK daily snapshot is added, create a separate source adapter rather than
 *   coupling that transport directly into the compliance engine
 * - If HH later supports broader canonical nutrient aggregation, expand the
 *   mapping here carefully
 */
class GetLocalDailyNutritionIntakeUseCase @Inject constructor(
    private val nutritionAggregateRepository: NutritionAggregateRepository
) {

    suspend operator fun invoke(
        date: LocalDate
    ): DailyNutritionIntake {
        val startMillis = startOfDayEpochMillis(date)
        val endMillis = startOfNextDayEpochMillis(date)

        val summaries = nutritionAggregateRepository
            .observeDailyNutritionSummariesInRange(
                startMillis = startMillis,
                endMillis = endMillis,
                timeZone = DomainTimePolicy.localTimeZone
            )
            .first()

        val summaryForDay = summaries.firstOrNull { it.date == date.toString() }

        return DailyNutritionIntake(
            date = startMillis,
            nutrients = summaryForDay.toCanonicalNutrientMap()
        )
    }

    private fun DailyNutritionSummary?.toCanonicalNutrientMap(): Map<String, Double> {
        if (this == null) return emptyMap()

        return buildMap {
            totalCalories?.let { put(CALORIES_KCAL, it) }
            put(PROTEIN_G, totalProtein)
            put(CARBS_G, totalCarbs)
            put(FAT_G, totalFat)
            sodium?.let { put(SODIUM_MG, it) }
            cholesterol?.let { put(CHOLESTEROL_MG, it) }
            fiber?.let { put(FIBER_G, it) }
        }
    }

    private fun startOfDayEpochMillis(date: LocalDate): Long =
        LocalDateTime(date = date, time = LocalTime(0, 0))
            .toInstant(DomainTimePolicy.localTimeZone)
            .toEpochMilliseconds()

    private fun startOfNextDayEpochMillis(date: LocalDate): Long =
        LocalDateTime(date = date, time = LocalTime(23, 59, 59, 999_999_999))
            .toInstant(DomainTimePolicy.localTimeZone)
            .toEpochMilliseconds() + 1L

    private companion object {
        const val CALORIES_KCAL = "CALORIES_KCAL"
        const val PROTEIN_G = "PROTEIN_G"
        const val CARBS_G = "CARBS_G"
        const val FAT_G = "FAT_G"
        const val SODIUM_MG = "SODIUM_MG"
        const val CHOLESTEROL_MG = "CHOLESTEROL_MG"
        const val FIBER_G = "FIBER_G"
    }
}