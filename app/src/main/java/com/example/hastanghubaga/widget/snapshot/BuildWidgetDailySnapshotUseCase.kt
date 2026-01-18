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

/**
 * Builds and persists a single "daily" snapshot for the home-screen widget.
 *
 * ## Responsibilities
 * - Loads *facts* (daily nutrient totals) from repositories.
 * - Loads *constraints/targets* (nutrition plan goals) for those facts.
 * - Loads *markers* (e.g., favorites) for those facts.
 * - Optionally includes a lightweight "Up Next" summary for context.
 * - Transforms all of the above into a [WidgetDailySnapshot].
 * - Persists the snapshot via [WidgetSnapshotStore] as a single atomic update.
 *
 * ## Non-Responsibilities (by design)
 * - Does **not** render RemoteViews.
 * - Does **not** handle widget tap actions, navigation, or deep links.
 * - Does **not** mutate user state (no logging, skipping, completing).
 * - Does **not** perform prioritization; it only summarizes available data.
 *
 * ## Why this exists
 * Widgets should be fast and dumb:
 * RemoteViews should only **read** a precomputed snapshot. All domain work happens here.
 *
 * @param nutrientTotalsRepository Source of daily totals (what the user has consumed).
 * @param nutritionPlanRepository Source of nutrition goals/targets (what the user aims for).
 * @param progressCalculator Calculates progress between consumed totals and goal targets.
 * @param ingredientPreferenceRepository Provides marker metadata (favorite, pinned, etc.).
 * @param widgetSnapshotStore Persisted snapshot storage (SharedPreferences + JSON).
 * @param observeNextUpcomingUseCase Provides the next scheduled/upcoming item (optional).
 * @param clock Clock dependency for deterministic "today" and consistent timestamps.
 */
class BuildWidgetDailySnapshotUseCase @Inject constructor(
    private val nutrientTotalsRepository: NutrientTotalsRepository,
    private val nutritionPlanRepository: NutritionPlanRepository,
    private val progressCalculator: NutritionProgressCalculator,
    private val ingredientPreferenceRepository: IngredientPreferenceRepository,
    private val widgetSnapshotStore: WidgetSnapshotStore,
    private val observeNextUpcomingUseCase: ObserveNextUpcomingUseCase,
    private val clock: Clock
) : BuildWidgetDailySnapshot {

    /**
     * Builds and saves a [WidgetDailySnapshot] for [day].
     *
     * The default [day] is computed from [clock] using [DomainTimePolicy] to ensure
     * consistent interpretation of "today" across the app.
     *
     * ### Output guarantees
     * - Always saves a snapshot (even when totals are empty).
     * - When totals are empty, inserts a single placeholder ingredient snapshot to
     *   keep widget rendering stable and non-null.
     *
     * @param day Target day for the snapshot. Defaults to "today" via [DomainTimePolicy].
     */
    override suspend operator fun invoke(
        day: LocalDate
    ) {
        // 1) Load totals (facts)
        val totals = nutrientTotalsRepository.getDailyTotals(day)
        Log.d(TAG, "invoke(day=$day) totals=${totals.size}")

        // 2) Load goals (constraints) for the ingredients present in totals
        val ingredientIds = totals.map { it.ingredientId }
        val goalsByIngredientId = nutritionPlanRepository.getGoalsForIngredientIds(ingredientIds)
        Log.d(TAG, "invoke(day=$day) goalsByIngredientId=${goalsByIngredientId.size}")

        // 3) Load markers (favorites, etc.) for the same ingredients
        val markersByIngredientId = ingredientPreferenceRepository.getForIngredientIds(ingredientIds)
        Log.d(TAG, "invoke(day=$day) markersByIngredientId=${markersByIngredientId.size}")

        // 4) Optional "Up Next" context (kept lightweight)
        val upNext = observeNextUpcomingUseCase()?.toUpNextSnapshot()

        // 5) Build ingredient snapshots (widget-friendly projection)
        val ingredientSnapshots =
            if (totals.isEmpty()) {
                listOf(placeholderIngredientSnapshot())
            } else {
                totals.map { total ->
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
            }

        Log.d(TAG, "invoke(day=$day) ingredientSnapshots=${ingredientSnapshots.size}")

        // 6) Assemble final snapshot
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

        // 7) Persist atomically
        widgetSnapshotStore.save(snapshot)

        // NOTE: optional debug verification; keep or remove as you prefer
        val loaded = widgetSnapshotStore.load()
        Log.d(TAG, "Reloaded snapshot != null? ${loaded != null}")
    }

    companion object {
        private const val TAG = "BuildWidgetDailySnapshot"
        private const val SCHEMA_VERSION = 1
    }
}
