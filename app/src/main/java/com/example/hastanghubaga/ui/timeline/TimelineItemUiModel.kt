package com.example.hastanghubaga.ui.timeline

import com.example.hastanghubaga.data.local.entity.meal.MealType
import com.example.hastanghubaga.data.local.entity.supplement.SupplementDoseUnit
import com.example.hastanghubaga.domain.model.activity.ActivityType
import com.example.hastanghubaga.domain.model.supplement.MealAwareDoseState
import kotlinx.datetime.LocalTime

/**
 * Represents a **UI-facing model** for a single entry in the Today timeline.
 *
 * ## Why this is a UI model
 * `TimelineItemUiModel` is intentionally **not** a domain model.
 * It exists to serve the needs of the presentation layer by:
 *
 * - Combining data from multiple domain sources
 *   (supplements, meals, activities)
 * - Normalizing heterogeneous items into a single renderable shape
 * - Precomputing UI-friendly fields such as:
 *   - icons
 *   - display strings
 *   - formatted times
 *
 * This allows the UI to remain:
 * - Stateless
 * - Declarative
 * - Free of business logic
 *
 * ## What this model may contain
 * - Display-ready text
 * - Preformatted dates/times
 * - UI-specific enums (icons, colors, row types)
 * - Stable domain identifiers (IDs)
 *
 * ## What this model must NOT contain
 * - Validation rules
 * - Persistence logic
 * - Repository references
 * - Business decisions
 *
 * ## Relationship to Domain Models
 * Domain models represent **truth**.
 * UI models represent **presentation**.
 *
 * Mapping from domain → UI models typically occurs in:
 * - Use cases (e.g. `BuildTodayTimelineUseCase`)
 * - Mappers owned by the presentation layer
 *
 * The domain layer must never depend on this type.
 *
 * ## Architectural Rationale
 * Keeping this model UI-facing prevents:
 * - Leaking presentation concerns into the domain
 * - Bloated domain entities
 * - UI conditionals based on domain internals
 *
 * It also enables the timeline to evolve visually
 * without impacting persistence or business rules.
 */
sealed interface TimelineItemUiModel {
    val id: Long
    val time: LocalTime
    val rowType: TodayUiRowType
    val key: String

    val title: String
    val subtitle: String?
    val isCompleted: Boolean

    val sendAlert: Boolean
    val alertOffsetMinutes: Int?
}

/**
 * UI-facing supplement timeline row.
 *
 * This usually represents a planned/scheduled supplement occurrence.
 *
 * ## Occurrence-aware logging
 * [occurrenceId] is optional because the app is transitioning from a log-first
 * timeline toward occurrence-aware reconciliation.
 *
 * When present, [occurrenceId] identifies the concrete planned supplement
 * occurrence that this row represents. This allows tap → dialog → confirm flows
 * to preserve a one-to-one link between:
 * - the planner/timeline row
 * - the eventual supplement log entry
 *
 * When absent, the row still behaves as a normal supplement timeline item, but
 * downstream logging flows may treat it as unlinked/manual.
 */
data class SupplementUiModel(
    override val id: Long,
    override val time: LocalTime,
    override val title: String,
    override val subtitle: String?,
    override val isCompleted: Boolean,
    override val sendAlert: Boolean = false,
    override val alertOffsetMinutes: Int? = null,

    val supplementId: Long,
    val scheduledTime: LocalTime,
    val doseState: MealAwareDoseState?,
    val defaultUnit: SupplementDoseUnit,
    val suggestedDose: Double,
    val occurrenceId: String? = null,
) : TimelineItemUiModel {

    override val rowType: TodayUiRowType =
        TodayUiRowType.SUPPLEMENT

    override val key: String =
        "SUPPLEMENT-$supplementId-$scheduledTime-${occurrenceId ?: "NO_OCCURRENCE"}"
}

data class ActivityUiModel(
    override val id: Long,
    override val time: LocalTime,
    override val title: String,
    override val subtitle: String?,
    override val isCompleted: Boolean,
    override val sendAlert: Boolean = false,
    override val alertOffsetMinutes: Int? = null,

    val activityId: Long,
    val activityType: ActivityType,
    val startTime: LocalTime,
    val endTime: LocalTime?,
    val intensity: Int?
) : TimelineItemUiModel {

    override val rowType: TodayUiRowType =
        TodayUiRowType.ACTIVITY

    override val key: String =
        "ACTIVITY-$activityId-$startTime"
}

data class MealUiModel(
    override val id: Long,
    override val time: LocalTime,
    override val title: String,
    override val subtitle: String?,
    override val isCompleted: Boolean,
    override val sendAlert: Boolean = false,
    override val alertOffsetMinutes: Int? = null,

    val mealId: Long,
    val mealType: MealType
) : TimelineItemUiModel {

    override val rowType: TodayUiRowType =
        TodayUiRowType.MEAL

    override val key: String =
        "MEAL-$mealId-$time"
}

/**
 * Read-only imported meal row.
 *
 * This is intentionally separate from [MealUiModel] so imported AK meals do not
 * go through HH native meal interactions such as "log a new meal now".
 */
data class ImportedMealUiModel(
    override val id: Long,
    override val time: LocalTime,
    override val title: String,
    override val subtitle: String?,
    override val isCompleted: Boolean,
    override val sendAlert: Boolean = false,
    override val alertOffsetMinutes: Int? = null,

    val importedMealId: Long,
    val mealType: MealType
) : TimelineItemUiModel {

    override val rowType: TodayUiRowType =
        TodayUiRowType.MEAL

    override val key: String =
        "IMPORTED_MEAL-$importedMealId-$time"
}

/**
 * A UI row representing an actual recorded supplement dose event.
 *
 * This is NOT the scheduled supplement row.
 * It exists so the timeline can show multiple logged doses (e.g., "extra dose")
 * as distinct rows, even if the supplement is scheduled only once.
 */
data class SupplementDoseLogUiModel(
    override val id: Long,
    override val time: LocalTime,
    override val title: String,
    override val subtitle: String?,
    override val isCompleted: Boolean,
    override val sendAlert: Boolean = false,
    override val alertOffsetMinutes: Int? = null,

    val supplementId: Long,
    val scheduledTime: LocalTime?,
    val amountText: String?,
    val unitText: String?
) : TimelineItemUiModel {

    override val rowType: TodayUiRowType = TodayUiRowType.SUPPLEMENT_DOSE_LOG

    override val key: String =
        "SUPPLEMENT_DOSE_LOG-$supplementId-$time-$id"
}