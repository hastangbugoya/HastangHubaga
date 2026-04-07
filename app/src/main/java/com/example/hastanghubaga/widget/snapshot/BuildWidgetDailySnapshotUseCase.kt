package com.example.hastanghubaga.widget.snapshot

import android.util.Log
import com.example.hastanghubaga.domain.repository.nutrition.NutrientTotalsRepository
import com.example.hastanghubaga.domain.repository.nutrition.NutritionPlanRepository
import com.example.hastanghubaga.domain.repository.widget.IngredientPreferenceRepository
import com.example.hastanghubaga.domain.time.DomainTimePolicy
import com.example.hastanghubaga.widget.calculator.NutritionProgressCalculator
import com.example.hastanghubaga.widget.model.WidgetDailySnapshot
import com.example.hastanghubaga.widget.model.WidgetDailySummary
import com.example.hastanghubaga.widget.model.WidgetIngredientMarkers
import com.example.hastanghubaga.widget.model.WidgetIngredientSnapshot
import com.example.hastanghubaga.widget.model.placeholderIngredientSnapshot
import com.example.hastanghubaga.widget.model.toUpNextSnapshot
import com.example.hastanghubaga.widget.nextup.ObserveNextUpcomingUseCase
import com.example.hastanghubaga.widget.snapshot.BuildWidgetDailySnapshot
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import javax.inject.Inject

class BuildWidgetDailySnapshotUseCase @Inject constructor(
    private val nutrientTotalsRepository: NutrientTotalsRepository,
    private val nutritionPlanRepository: NutritionPlanRepository,
    private val progressCalculator: NutritionProgressCalculator,
    private val ingredientPreferenceRepository: IngredientPreferenceRepository,
    private val widgetSnapshotStore: WidgetSnapshotStore,
    private val observeNextUpcomingUseCase: ObserveNextUpcomingUseCase,
    private val clock: Clock
) : BuildWidgetDailySnapshot {

    override suspend operator fun invoke(
        day: LocalDate
    ) {
        val totals = nutrientTotalsRepository.getDailyTotals(day)
        Log.d(TAG, "invoke(day=$day) totals=${totals.size}")

        // 🔥 NEW: fetch active plans instead of ingredientId lookup
        val activePlans = nutritionPlanRepository.getActivePlans()

        // Flatten all goals from all active plans
        val allGoals = activePlans.flatMap { plan ->
            nutritionPlanRepository.getGoalsForPlan(plan.id)
        }

        // Map by nutrientKey (TEMP: using ingredientId.toString())
        val goalsByKey = allGoals.associateBy { it.nutrientKey }

        Log.d(TAG, "invoke(day=$day) goals=${goalsByKey.size}")

        val ingredientIds = totals.map { it.ingredientId }

        val markersByIngredientId =
            ingredientPreferenceRepository.getForIngredientIds(ingredientIds)

        val upNext = observeNextUpcomingUseCase()?.toUpNextSnapshot()

        val ingredientSnapshots =
            if (totals.isEmpty()) {
                listOf(placeholderIngredientSnapshot())
            } else {
                totals.map { total ->

                    val key = total.ingredientId.toString()

                    WidgetIngredientSnapshot(
                        ingredientId = total.ingredientId,
                        name = total.name,
                        unit = total.unit.name,
                        progress = progressCalculator.calculate(
                            consumed = total.amount,
                            goal = goalsByKey[key]
                        ),
                        markers = markersByIngredientId[total.ingredientId]?.let {
                            WidgetIngredientMarkers(favorite = it.isFavorite)
                        }
                    )
                }
            }

        Log.d(TAG, "invoke(day=$day) ingredientSnapshots=${ingredientSnapshots.size}")

        val generatedAt = DomainTimePolicy.todayLocal(clock).toString()

        val snapshot = WidgetDailySnapshot(
            schemaVersion = SCHEMA_VERSION,
            generatedAt = generatedAt,
            day = day.toString(),
            summary = WidgetDailySummary(
                totalIngredients = ingredientSnapshots.size
            ),
            upNext = upNext,
            ingredients = ingredientSnapshots
        )

        Log.d(TAG, "Saving WidgetDailySnapshot(schemaVersion=${snapshot.schemaVersion}, day=${snapshot.day})")

        widgetSnapshotStore.save(snapshot)

        val loaded = widgetSnapshotStore.load()
        Log.d(TAG, "Reloaded snapshot != null? ${loaded != null}")
    }

    companion object {
        private const val TAG = "BuildWidgetDailySnapshot"
        private const val SCHEMA_VERSION = 1
    }
}