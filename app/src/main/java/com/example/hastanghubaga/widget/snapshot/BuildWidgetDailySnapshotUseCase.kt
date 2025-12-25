package com.example.hastanghubaga.widget.snapshot

import com.example.hastanghubaga.domain.repository.supplement.IngredientPreferenceRepository
import com.example.hastanghubaga.widget.aggregate.WidgetAggregateProvider
import com.example.hastanghubaga.widget.model.WidgetDailySnapshot
import com.example.hastanghubaga.widget.model.WidgetDailySummary
import com.example.hastanghubaga.widget.model.WidgetIngredientProgress
import com.example.hastanghubaga.widget.model.WidgetIngredientSnapshot
import com.example.hastanghubaga.widget.model.WidgetIngredientMarkers
import java.time.Clock
import java.time.LocalDate

import javax.inject.Inject
import kotlin.math.roundToInt

class BuildWidgetDailySnapshotUseCase @Inject constructor(
    private val aggregateProviders: Set<@JvmSuppressWildcards WidgetAggregateProvider>,
    private val ingredientPreferenceRepository: IngredientPreferenceRepository,
    private val widgetSnapshotStore: WidgetSnapshotStore,
    private val clock: Clock
) {

    suspend operator fun invoke(
        day: LocalDate = LocalDate.now(clock)
    ) {
        // 1. Collect aggregates
        val summary = aggregateProviders
            .flatMap { it.getDailySummaries(day) }

        // 2. Fetch preferences once
        val preferencesById =
            ingredientPreferenceRepository.getForIngredientIds(
                summary.map { it.ingredientId }
            )

        // 3. Build ingredient snapshots
        val ingredientSnapshots = summary.map { summary ->
            val preference = preferencesById[summary.ingredientId]

            WidgetIngredientSnapshot(
                ingredientId = summary.ingredientId,
                name = summary.name,
                unit = summary.unit,
                progress = WidgetIngredientProgress(
                    current = round(summary.totalAmount),
                    target = null,
                    percent = null,
                    status = null,
                    exceeded = null
                ),
                markers = preference?.let {
                    WidgetIngredientMarkers(
                        favorite = it.isFavorite
                    )
                }
            )
        }

        // 4. Build snapshot
        val snapshot = WidgetDailySnapshot(
            schemaVersion = 1,
            generatedAt = clock.instant().toString(),
            day = day.toString(),
            summary = WidgetDailySummary(
                totalIngredients = ingredientSnapshots.size
            ),
            upNext = null, // integrated later
            ingredients = ingredientSnapshots
        )

        // 5. Persist atomically
        widgetSnapshotStore.save(snapshot)
    }

    private fun round(value: Double): Double =
        (value * 10).roundToInt() / 10.0
}
