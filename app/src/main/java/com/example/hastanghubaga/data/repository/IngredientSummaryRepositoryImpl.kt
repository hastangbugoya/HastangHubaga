package com.example.hastanghubaga.data.repository

import com.example.hastanghubaga.data.local.dao.widget.IngredientContributionDao
import com.example.hastanghubaga.domain.repository.widget.IngredientSummaryRepository
import com.example.hastanghubaga.widget.model.WidgetDailyIngredientSummary
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

class IngredientSummaryRepositoryImpl @Inject constructor(
    private val dao: IngredientContributionDao,
    private val clock: Clock
) : IngredientSummaryRepository {

    override suspend fun getDailyIngredientSummary(
        day: LocalDate
    ): List<WidgetDailyIngredientSummary> {

        val zone = clock.zone
        val start = day
            .atStartOfDay(zone)
            .toInstant()
            .toEpochMilli()

        val end = day
            .plusDays(1)
            .atStartOfDay(zone)
            .toInstant()
            .toEpochMilli()

        return dao.getDailySummary(start, end)
            .map {
                WidgetDailyIngredientSummary(
                    ingredientId = it.ingredientId,
                    name = it.name,
                    unit = it.unit,
                    totalAmount = it.totalAmount
                )
            }
    }
}
