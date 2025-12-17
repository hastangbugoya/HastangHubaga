package com.example.hastanghubaga.domain.usecase.meal

import com.example.hastanghubaga.domain.model.meal.Meal
import com.example.hastanghubaga.domain.repository.meal.MealRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

/**
 * Returns all meals logged for a given date.
 *
 * This use case:
 * - Observes meals reactively
 * - Applies date scoping only
 * - Does NOT perform sorting or timeline logic
 *
 * Ordering and presentation concerns are handled elsewhere.
 */
class GetMealsForDateUseCase @Inject constructor(
    private val mealRepository: MealRepository
) {

    operator fun invoke(date: LocalDate): Flow<List<Meal>> {
        return mealRepository.observeMealsForDate(date)
    }
}
