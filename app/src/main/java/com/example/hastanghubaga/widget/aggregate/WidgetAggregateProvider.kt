package com.example.hastanghubaga.widget.aggregate

import com.example.hastanghubaga.widget.model.WidgetDailyIngredientSummary
import java.time.LocalDate


interface WidgetAggregateProvider {
    suspend fun getDailySummaries(day: LocalDate): List<WidgetDailyIngredientSummary>
}
