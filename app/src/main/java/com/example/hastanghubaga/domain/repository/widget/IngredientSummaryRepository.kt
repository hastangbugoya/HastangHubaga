package com.example.hastanghubaga.domain.repository.widget

import com.example.hastanghubaga.widget.model.WidgetDailyIngredientSummary
import java.time.LocalDate

interface IngredientSummaryRepository {
    suspend fun getDailyIngredientSummary(
        day: LocalDate
    ): List<WidgetDailyIngredientSummary>
}
