package com.example.hastanghubaga.domain.usecase.meal

import com.example.hastanghubaga.domain.model.meal.Meal
import com.example.hastanghubaga.domain.repository.meal.MealRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import javax.inject.Inject

/**
 * Provides the HH meal template lookup list used by the day-level meal
 * occurrence pipeline.
 *
 * Important architectural rule:
 * - MealEntity is a reusable template, not a date-scoped event row
 * - date-specific meal presence comes from MealOccurrenceEntity
 * - this use case supplies the template side needed to resolve
 *   occurrence.mealId -> Meal during timeline building
 *
 * The [date] parameter is retained because this use case participates in the
 * date-scoped timeline pipeline, even though the template source itself is not
 * filtered by date.
 */
class GetMealsForDateUseCase @Inject constructor(
    private val mealRepository: MealRepository
) {

    operator fun invoke(date: LocalDate): Flow<List<Meal>> {
        return mealRepository.observeAll()
    }
}