package com.example.hastanghubaga.data.repository

import com.example.hastanghubaga.data.local.dao.supplement.SupplementDailyLogDao
import com.example.hastanghubaga.data.local.dao.supplement.SupplementEntityDao
import com.example.hastanghubaga.domain.model.nutrition.DailyIngredientSummary
import com.example.hastanghubaga.domain.repository.nutrition.NutrientTotalsRepository
import kotlinx.datetime.LocalDate
import javax.inject.Inject

class NutrientTotalsRepositoryImpl @Inject constructor(
    private val supplementDailyLogDao: SupplementDailyLogDao,
    private val supplementDao: SupplementEntityDao
) : NutrientTotalsRepository {

    override suspend fun getDailyTotals(
        day: LocalDate
    ): List<DailyIngredientSummary> {

        val logs = supplementDailyLogDao
            .getDoseLogsForDayOnce(day.toString())

        val supplements = supplementDao
            .getAllSupplementsWithIngredients()

        val supplementLookup =
            supplements.associateBy { it.supplement.id }

        val totals = mutableMapOf<Long, DailyIngredientSummary>()

        logs.forEach { log ->
            val supplement =
                supplementLookup[log.supplementId] ?: return@forEach

            supplement.ingredients.forEach { item ->
                val ingredientId = item.ingredient.ingredientId
                val taken =
                    item.ingredient.amountPerServing * log.actualServingTaken

                val entry = totals.getOrPut(ingredientId) {
                    DailyIngredientSummary(
                        ingredientId = ingredientId,
                        name = item.info.name,
                        unit = item.ingredient.unit,
                        amount = 0.0,
                    )
                }

                totals[ingredientId] =
                    entry.copy(amount = entry.amount + taken)
            }
        }

        return totals.values.toList()
    }
}
