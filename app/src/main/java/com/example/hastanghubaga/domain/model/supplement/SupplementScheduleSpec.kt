package com.example.hastanghubaga.domain.model.supplement

import com.example.hastanghubaga.data.local.entity.meal.MealType
import com.example.hastanghubaga.domain.schedule.model.TimeAnchor
import kotlinx.datetime.LocalTime

sealed interface SupplementScheduleSpec {

    /**
     * User explicitly chooses times of day.
     * Example: 07:00, 12:00, 16:00
     */
    data class FixedTimes(
        val times: List<LocalTime>
    ) : SupplementScheduleSpec

    /**
     * User anchors supplement to one or more meals.
     * Example:
     * - With breakfast
     * - With breakfast and dinner
     * - 30 min after lunch
     */
    data class MealAnchored(
        val mealTypes: Set<MealType>,
        val offsetMinutes: Int = 0
    ) : SupplementScheduleSpec

    /**
     * Legacy/simple anchored representation.
     *
     * This shape is still useful for compatibility paths where all anchors
     * share the same offset.
     *
     * Example:
     * - Wakeup and sleep, both with offset 0
     * - Before workout and after workout, both with offset -15
     *
     * For persisted anchored schedule rows with per-row offsets/labels/order,
     * prefer [AnchoredRows].
     */
    data class Anchored(
        val anchors: Set<TimeAnchor>,
        val offsetMinutes: Int = 0
    ) : SupplementScheduleSpec

    /**
     * Rich anchored schedule representation preserving one row per anchor entry.
     *
     * WHY THIS EXISTS
     *
     * Persisted anchored supplement schedules are modeled as child rows, where
     * each row may carry its own:
     * - anchor
     * - offsetMinutes
     * - optional label
     * - sortOrder
     *
     * A single shared-offset [Anchored] shape is not rich enough to preserve
     * that information faithfully when multiple anchored rows exist.
     *
     * This shape allows the scheduling engine to keep anchored schedule intent
     * lossless until same-day concrete times are resolved.
     */
    data class AnchoredRows(
        val rows: List<AnchoredRow>
    ) : SupplementScheduleSpec {

        init {
            require(rows.isNotEmpty()) {
                "AnchoredRows must contain at least one anchored row."
            }
        }
    }
}

/**
 * One anchored schedule row inside [SupplementScheduleSpec.AnchoredRows].
 *
 * This mirrors the meaningful domain parts of one persisted anchored child row
 * without exposing Room entities outside the data layer.
 */
data class AnchoredRow(
    val anchor: TimeAnchor,
    val offsetMinutes: Int = 0,
    val label: String? = null,
    val sortOrder: Int = 0
)