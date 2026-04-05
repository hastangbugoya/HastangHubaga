package com.example.hastanghubaga.domain.usecase.meal

import com.example.hastanghubaga.domain.model.meal.Meal
import com.example.hastanghubaga.domain.repository.meal.MealRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.LocalDate
import javax.inject.Inject

/**
 * Transitional use case.
 *
 * Meals are no longer timestamped rows, so we cannot query meals "for a date"
 * from the template table anymore.
 *
 * Future direction:
 * - Replace this with:
 *   - planned meal occurrences (from schedule)
 *   - actual meal logs (from log table)
 *
 * For now, return empty until the new pipeline is implemented.
 */
class GetMealsForDateUseCase @Inject constructor(
    private val mealRepository: MealRepository
) {

    operator fun invoke(date: LocalDate): Flow<List<Meal>> {
        return flowOf(emptyList())
    }
}