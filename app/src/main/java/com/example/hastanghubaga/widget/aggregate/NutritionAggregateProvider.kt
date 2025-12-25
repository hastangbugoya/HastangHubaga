package com.example.hastanghubaga.widget.aggregate

import com.example.hastanghubaga.domain.repository.widget.IngredientSummaryRepository
import com.example.hastanghubaga.widget.model.WidgetDailyIngredientSummary
import java.time.LocalDate
import javax.inject.Inject


class NutritionAggregateProvider @Inject constructor(
    private val ingredientSummaryRepository: IngredientSummaryRepository
) : WidgetAggregateProvider {

    override suspend fun getDailySummaries(day: LocalDate): List<WidgetDailyIngredientSummary> =
        ingredientSummaryRepository.getDailyIngredientSummary(day)
}
