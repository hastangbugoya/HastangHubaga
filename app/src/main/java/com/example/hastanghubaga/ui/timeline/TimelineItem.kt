package com.example.hastanghubaga.ui.timeline

import com.example.hastanghubaga.data.local.entity.meal.AkImportedMealEntity
import com.example.hastanghubaga.data.local.entity.supplement.SupplementDoseUnit
import com.example.hastanghubaga.domain.model.meal.Meal
import com.example.hastanghubaga.domain.model.supplement.MealAwareDoseState
import com.example.hastanghubaga.domain.schedule.model.TimeAnchor
import kotlinx.datetime.LocalTime

sealed interface TimelineItem {
    /**
     * Canonical user-facing placement time for this row in the Today timeline.
     *
     * This is the time used for:
     * - sorting
     * - display
     * - cross-type timeline merging
     *
     * Time resolution belongs upstream to the source-specific scheduling / timing
     * pipeline. The timeline layer must treat this as already-resolved placement
     * time and should not reinterpret source model timestamps.
     */
    val time: LocalTime

    /**
     * UI-ready supplement ingredient row for expanded timeline card display.
     *
     * Important:
     * - [name] is the display ingredient name already resolved upstream
     * - [amountText] is the display-ready amount/unit text
     * - formatting/styling is handled by UI rendering, not by flattening into a
     *   single string here
     */
    data class TimelineIngredientUi(
        val name: String,
        val amountText: String
    )

    /**
     * Planned supplement timeline row.
     *
     * This row now represents the PLANNED side of supplement behavior:
     * one concrete supplement occurrence for the selected day.
     *
     * Canonical identity:
     * - [occurrenceId] is the stable planner occurrence ID
     * - one planned occurrence = one planned timeline row
     *
     * Architectural intent:
     * - planned rows come from the planned occurrence ledger
     * - actual logged doses remain separate rows
     * - reconciliation happens by occurrence ID
     *
     * Important:
     * - [time] is the canonical timeline placement time
     * - [scheduledTime] preserves the original planned time context
     * - in the common case they are the same
     * - UI/logging flows should preserve [occurrenceId] when this planned row
     *   is logged as an actual dose
     *
     * This model intentionally avoids embedding rich schedule-resolution
     * objects directly into the timeline layer. The timeline should carry the
     * planner occurrence identity and the display/logging data it needs, but
     * should not remain coupled to upstream schedule-resolution internals.
     */
    data class SupplementTimelineItem(
        override val time: LocalTime,
        val occurrenceId: String,
        val supplementId: Long,
        val title: String,
        val subtitle: String?,
        val defaultUnit: SupplementDoseUnit,
        val suggestedDose: Double,
        val doseState: MealAwareDoseState? = null,
        val scheduledTime: LocalTime = time,
        val isTaken: Boolean = false,
        val ingredients: List<TimelineIngredientUi> = emptyList()
    ) : TimelineItem

    /**
     * Activity timeline row.
     *
     * This row may represent either:
     * - a PLANNED activity occurrence for the selected day
     * - an ACTUAL logged activity session
     *
     * Canonical identity:
     * - [occurrenceId] is the planner occurrence identity when available
     * - planned rows always have a concrete occurrence ID
     * - actual rows may preserve that same occurrence ID when the log fulfilled
     *   a planned occurrence, or use a synthetic fallback identity for extra logs
     *
     * Completion semantics:
     * - [isCompleted] = false for planned rows
     * - [isCompleted] = true for actual logged rows
     *
     * Important:
     * - [time] is the canonical resolved timeline placement time
     * - [scheduledTime] preserves the original planned or actual source time
     * - [isWorkout] is the occurrence/log snapshot used for display only
     */
    data class ActivityTimelineItem(
        override val time: LocalTime,
        val occurrenceId: String,
        val activityId: Long,
        val title: String,
        val subtitle: String? = null,
        val isWorkout: Boolean = false,
        val scheduledTime: LocalTime = time,
        val isCompleted: Boolean = false
    ) : TimelineItem

    /**
     * Native HH meal timeline row.
     *
     * This row may represent either:
     * - a PLANNED meal occurrence for the selected day
     * - an ACTUAL logged HH meal
     *
     * Canonical identity:
     * - [occurrenceId] is the planner occurrence identity when available
     * - planned rows always preserve the concrete occurrence ID
     * - actual rows may preserve that same occurrence ID when the log fulfilled
     *   a planned occurrence, or use a synthetic fallback identity for extra logs
     *
     * Completion semantics:
     * - [isCompleted] = false for planned rows
     * - [isCompleted] = true for actual logged rows
     *
     * Important:
     * - [resolvedAnchor] is derived anchor behavior only
     * - It does NOT change the meal's actual type
     * - It does NOT affect current timeline rendering or ordering
     * - It prepares meals to act as anchor providers later
     * - [scheduledTime] preserves the original planned or actual source time
     */
    data class MealTimelineItem(
        override val time: LocalTime,
        val occurrenceId: String,
        val meal: Meal,
        val scheduledTime: LocalTime = time,
        val isCompleted: Boolean = false,
        val resolvedAnchor: TimeAnchor? = null
    ) : TimelineItem

    /**
     * Read-only AK imported meal timeline row.
     *
     * Important:
     * - This is NOT a native HH meal
     * - This is backed by ak_imported_meals materialization only
     * - Do not use this to imply linking, merging, or assignment to HH meals
     */
    data class ImportedMealTimelineItem(
        override val time: LocalTime,
        val meal: AkImportedMealEntity
    ) : TimelineItem

    /**
     * Actual recorded supplement dose event.
     *
     * This is the ACTUAL side of supplement behavior.
     *
     * Important:
     * - this is not the planned row
     * - multiple actual logs may exist for the same supplement on the same day
     * - [scheduledTime] is informational only and must not replace [time] for
     *   timeline placement
     */
    data class SupplementDoseLogTimelineItem(
        val doseLogId: Long,
        val supplementId: Long,
        val title: String,
        override val time: LocalTime,
        val amount: Double?,
        val unit: String?,
        val scheduledTime: LocalTime? = null,
        val ingredients: List<TimelineIngredientUi> = emptyList()
    ) : TimelineItem
}