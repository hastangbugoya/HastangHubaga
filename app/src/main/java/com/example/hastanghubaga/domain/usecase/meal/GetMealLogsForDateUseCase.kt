package com.example.hastanghubaga.domain.usecase.meal

import com.example.hastanghubaga.domain.model.meal.MealLog
import com.example.hastanghubaga.domain.repository.meal.MealLogRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

/**
 * Observes actual logged HH meals for a single date.
 *
 * Canonical meal model:
 * - MealEntity = template
 * - MealOccurrenceEntity = planned occurrence
 * - MealLogEntity = actual consumed meal
 *
 * This is the ACTUAL-side read path for the meal timeline merge.
 */
class GetMealLogsForDateUseCase @Inject constructor(
    private val mealLogRepository: MealLogRepository
) {
    operator fun invoke(
        date: LocalDate
    ): Flow<List<MealLog>> {
        return mealLogRepository.observeMealLogsForDate(date)
    }
}