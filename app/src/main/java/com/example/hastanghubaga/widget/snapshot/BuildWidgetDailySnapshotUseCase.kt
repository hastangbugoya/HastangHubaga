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
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import javax.inject.Inject

class BuildWidgetDailySnapshotUseCase @Inject constructor(
    private val nutrientTotalsRepository: NutrientTotalsRepository,
    private val nutritionPlanRepository: NutritionPlanRepository,
    private val progressCalculator: NutritionProgressCalculator,
    private val ingredientPreferenceRepository: IngredientPreferenceRepository,
    private val widgetSnapshotStore: WidgetSnapshotStore,
    private val clock: Clock
) {

    suspend operator fun invoke(
        day: LocalDate = DomainTimePolicy.todayLocal(clock)
    ) {
        // 1. Load totals (facts)
        val totals = nutrientTotalsRepository.getDailyTotals(day)
        Log.d("Meow","BuildWidgetDailySnapshotUseCase> invoke totals: ${totals.size}")
        // 2. Load goals (constraints)
        val goalsByIngredientId =
            nutritionPlanRepository.getGoalsForIngredientIds(
                totals.map { it.ingredientId }
            )
        Log.d("Meow","BuildWidgetDailySnapshotUseCase> invoke goalsByIngredientId: ${goalsByIngredientId.size}")
        // 3. Load markers (favorites, etc.)
        val markersByIngredientId =
            ingredientPreferenceRepository.getForIngredientIds(
                totals.map { it.ingredientId }
            )
        Log.d("Meow","BuildWidgetDailySnapshotUseCase> invoke markersByIngredientId: ${markersByIngredientId.size}")
        // 4. Build widget ingredient snapshots
        val ingredientSnapshots = totals.map { total ->
            WidgetIngredientSnapshot(
                ingredientId = total.ingredientId,
                name = total.name,
                unit = total.unit.name,
                progress = progressCalculator.calculate(
                    consumed = total.amount,
                    goal = goalsByIngredientId[total.ingredientId]
                ),
                markers = markersByIngredientId[total.ingredientId]?.let {
                    WidgetIngredientMarkers(favorite = it.isFavorite)
                }
            )
        }
        Log.d("Meow","BuildWidgetDailySnapshotUseCase> invoke ingredientSnapshots: ${ingredientSnapshots.size}")

        // 5. Assemble final snapshot
        val snapshot = WidgetDailySnapshot(
            schemaVersion = 1,
            generatedAt = DomainTimePolicy.todayLocal(clock).toString(),
            day = day.toString(),
            summary = WidgetDailySummary(
                totalIngredients = ingredientSnapshots.size
            ),
            upNext = null, // wired later
            ingredients = ingredientSnapshots
        )
        Log.d("Meow" ,"WidgetSnapshot > Saved snapshot: ${snapshot.toString()}")
        // 6. Persist atomically
        widgetSnapshotStore.save(snapshot)
        val loaded = widgetSnapshotStore.load()
        Log.d("Meow" ,"WidgetSnapshot > reloaded snapshot = $loaded")
    }
}

