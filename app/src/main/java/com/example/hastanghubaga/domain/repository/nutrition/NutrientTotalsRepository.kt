package com.example.hastanghubaga.domain.repository.nutrition

import com.example.hastanghubaga.domain.model.nutrition.DailyIngredientSummary
import kotlinx.datetime.LocalDate

interface NutrientTotalsRepository {
    suspend fun getDailyTotals(day: LocalDate): List<DailyIngredientSummary>
}