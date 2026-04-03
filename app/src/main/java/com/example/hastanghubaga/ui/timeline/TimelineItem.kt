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
        val isTaken: Boolean = false
    ) : TimelineItem

    /**
     * Planned activity timeline row.
     *
     * This row represents the PLANNED side of activity behavior:
     * one concrete activity occurrence for the selected day.
     *
     * Canonical identity:
     * - [occurrenceId] is the stable planner occurrence ID
     * - one planned occurrence = one planned timeline row
     *
     * Architectural intent:
     * - planned rows come from the planned activity occurrence ledger
     * - future actual activity logs remain separate rows
     * - reconciliation should later happen by occurrence ID, mirroring supplements
     *
     * Important:
     * - [time] is the canonical resolved timeline placement time
     * - [scheduledTime] preserves the original planned time context
     * - [isWorkout] is the occurrence-level planner snapshot, not the template default
     */
    data class ActivityTimelineItem(
        override val time: LocalTime,
        val occurrenceId: String,
        val activityId: Long,
        val title: String,
        val subtitle: String? = null,
        val isWorkout: Boolean = false,
        val scheduledTime: LocalTime = time
    ) : TimelineItem

    /**
     * Native HH meal timeline row.
     *
     * Important:
     * - [resolvedAnchor] is derived anchor behavior only
     * - It does NOT change the meal's actual type
     * - It does NOT affect current timeline rendering or ordering
     * - It prepares meals to act as anchor providers later
     */
    data class MealTimelineItem(
        override val time: LocalTime,
        val meal: Meal,
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
        val scheduledTime: LocalTime? = null
    ) : TimelineItem
}