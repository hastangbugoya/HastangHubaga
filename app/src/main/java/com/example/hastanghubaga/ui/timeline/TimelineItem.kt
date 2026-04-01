package com.example.hastanghubaga.ui.timeline

import com.example.hastanghubaga.data.local.entity.meal.AkImportedMealEntity
import com.example.hastanghubaga.domain.model.activity.Activity
import com.example.hastanghubaga.domain.model.meal.Meal
import com.example.hastanghubaga.domain.model.supplement.ResolvedSupplementScheduleEntry
import com.example.hastanghubaga.domain.model.supplement.SupplementWithUserSettings
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
     * Planned/scheduled supplement timeline row.
     *
     * This row represents a supplement occurrence shown in the Today timeline.
     *
     * ## Schedule-aware behavior
     * [resolvedScheduleEntry] preserves the concrete same-day scheduling output
     * that produced this row when available.
     *
     * This enables the timeline to retain:
     * - schedule identity
     * - timing source (fixed / anchored / legacy)
     * - row-level anchor metadata
     * - future linkage to stricter recurrence behavior
     *
     * It is nullable for backward compatibility while older callers still build
     * supplement timeline rows from flat [SupplementWithUserSettings.scheduledTimes].
     *
     * ## Occurrence-aware reconciliation
     * [occurrenceId] is optional because the app is transitioning from a
     * schedule/log split toward explicit occurrence ↔ log linkage.
     *
     * When present, [occurrenceId] identifies the concrete planner occurrence
     * that this supplement row represents. This enables:
     * - one-to-one reconciliation with a logged dose
     * - multi-dose-per-day support
     * - future promotion of ad-hoc doses into first-class planner items
     *
     * When absent, the row still behaves as a normal scheduled supplement item
     * but downstream logging/reconciliation may treat it as unlinked.
     */
    data class SupplementTimelineItem(
        override val time: LocalTime,
        val isTaken: Boolean = false,
        val supplement: SupplementWithUserSettings,
        val resolvedScheduleEntry: ResolvedSupplementScheduleEntry? = null,
        val occurrenceId: String? = null
    ) : TimelineItem

    data class ActivityTimelineItem(
        override val time: LocalTime,
        val activity: Activity
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