package com.example.hastanghubaga.widget.model
/**
 * Immutable snapshot representing all data required to render the daily app widget.
 *
 * This snapshot is:
 * - Built by [BuildWidgetDailySnapshotUseCase]
 * - Persisted (e.g. SharedPreferences / DataStore) as a single atomic JSON blob
 * - Consumed read-only by widget rendering code
 *
 * The snapshot is intentionally denormalized and projection-based:
 * - No business logic lives here
 * - All values are precomputed and formatted for display
 * - No database access is required at render time
 *
 * Schema evolution is handled explicitly via [schemaVersion].
 */
data class WidgetDailySnapshot(

    /**
     * Snapshot schema version.
     *
     * Used to support backward-compatible migrations when the snapshot
     * structure changes (e.g. adding new sections, fields, or metrics).
     *
     * Widget code should be able to detect unsupported versions and
     * trigger a rebuild rather than crashing.
     */
    val schemaVersion: Int,

    /**
     * ISO-8601 timestamp (UTC) indicating when this snapshot was generated.
     *
     * This is primarily used for:
     * - Debugging and validation
     * - Determining staleness
     * - Ensuring widget updates reflect recent data
     */
    val generatedAt: String,

    /**
     * ISO-8601 date (local) this snapshot represents (e.g. "2025-12-24").
     *
     * All aggregated values in this snapshot (nutrition totals, progress,
     * scheduling) are scoped to this day.
     */
    val day: String,

    /**
     * High-level daily summary information derived from underlying data.
     *
     * This may include counts, aggregate indicators, or future extensions
     * such as calorie totals or completion states.
     */
    val summary: WidgetDailySummary,

    /**
     * Snapshot of the next scheduled timeline item (e.g. supplement alert).
     *
     * Nullable by design:
     * - `null` indicates there is nothing upcoming ("All done" state)
     * - Widget UI should render a completed/empty state accordingly
     */
    val upNext: UpNextSnapshot?,

    /**
     * List of per-ingredient / per-nutrient projections for the day.
     *
     * Each entry represents:
     * - Aggregated daily totals (from all sources: meals, supplements, etc.)
     * - Precomputed progress values (already rounded / normalized)
     * - Optional marker metadata (e.g. favorites)
     *
     * This list is ordered and ready for direct widget rendering.
     */
    val ingredients: List<WidgetIngredientSnapshot>
)
