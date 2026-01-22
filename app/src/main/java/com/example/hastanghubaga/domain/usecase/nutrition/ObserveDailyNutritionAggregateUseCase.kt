package com.example.hastanghubaga.domain.usecase.nutrition

import com.example.hastanghubaga.domain.model.meal.MealNutrition
import com.example.hastanghubaga.domain.repository.meal.MealRepository
import com.example.hastanghubaga.domain.repository.supplement.SupplementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/**
 * Computes the aggregated daily nutrition totals from all meals and
 * nutrition-bearing supplements consumed on a given date.
 *
 * Missing values are treated as zero and omitted nutrients remain null
 * if no contributing source provides them.
 */
class ObserveDailyNutritionAggregateUseCase @Inject constructor(
    private val mealRepository: MealRepository,
    private val supplementRepository: SupplementRepository
) {

    operator fun invoke(date: Long): Flow<MealNutrition> {
        return combine(
            mealRepository.observeMealNutritionForDate(date),
            supplementRepository.observeSupplementNutritionForDate(date)
        ) { meals, supplements ->
            aggregate(meals + supplements)
        }
    }

    private fun aggregate(items: List<MealNutrition>): MealNutrition {
        fun sum(selector: (MealNutrition) -> Double?): Double? {
            val values = items.mapNotNull(selector)
            return if (values.isEmpty()) null else values.sum()
        }

        return MealNutrition(
            protein = sum { it.protein },
            carbs = sum { it.carbs },
            fat = sum { it.fat },
            calories = sum { it.calories },
            sodium = sum { it.sodium },
            cholesterol = sum { it.cholesterol },
            fiber = sum { it.fiber }
        )
    }
}
