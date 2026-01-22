package com.example.hastanghubaga.domain.usecase.nutrition

import com.example.hastanghubaga.domain.model.meal.DailyNutritionSummary
import com.example.hastanghubaga.domain.repository.nutrition.NutritionAggregateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import javax.inject.Inject

/**
 * Observes per-day summed nutrition totals for a given month.
 * Data is derived from meals + supplement dose logs (actual consumption).
 */
class ObserveMonthlyNutritionSummariesUseCase @Inject constructor(
    private val repository: NutritionAggregateRepository
) {

    operator fun invoke(
        year: Int,
        month: Int,
        timeZone: TimeZone = TimeZone.currentSystemDefault()
    ): Flow<List<DailyNutritionSummary>> {

        val startDate = LocalDate(year, month, 1)
        val endDateExclusive = startDate.plus(DatePeriod(months = 1))

        val startMillis = startDate.atStartOfDayIn(timeZone).toEpochMilliseconds()
        val endMillis = endDateExclusive.atStartOfDayIn(timeZone).toEpochMilliseconds()

        return repository.observeDailyNutritionSummariesInRange(
            startMillis = startMillis,
            endMillis = endMillis,
            timeZone = timeZone
        )
    }
}
