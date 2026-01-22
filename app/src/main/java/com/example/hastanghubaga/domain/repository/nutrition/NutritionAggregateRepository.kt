package com.example.hastanghubaga.domain.repository.nutrition

import com.example.hastanghubaga.domain.model.meal.DailyNutritionSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.TimeZone

interface NutritionAggregateRepository {
    fun observeDailyNutritionSummariesInRange(
        startMillis: Long,
        endMillis: Long,
        timeZone: TimeZone
    ): Flow<List<DailyNutritionSummary>>
}