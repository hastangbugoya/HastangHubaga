package com.example.hastanghubaga.ui.timeline



import com.example.hastanghubaga.data.local.entity.supplement.SupplementDoseUnit
import com.example.hastanghubaga.domain.model.activity.ActivityType
import com.example.hastanghubaga.domain.model.meal.MealType
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

) : TimelineItemUiModel {

    override val rowType: TodayUiRowType =
        TodayUiRowType.SUPPLEMENT

    override val key: String =
        "SUPPLEMENT-$supplementId-$scheduledTime"
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
 * A UI row representing an actual recorded supplement dose event.
 *
 * This is NOT the scheduled supplement row.
 * It exists so the timeline can show multiple logged doses (e.g., "extra dose")
 * as distinct rows, even if the supplement is scheduled only once.
 */
data class SupplementDoseLogUiModel(
    override val id: Long,                // doseLogId if you have one; otherwise synthetic
    override val time: LocalTime,          // when the dose was taken
    override val title: String,            // supplement name
    override val subtitle: String?,        // "1 cap" / "extra dose" / etc.
    override val isCompleted: Boolean,     // usually true (a log row is "done")
    override val sendAlert: Boolean = false,
    override val alertOffsetMinutes: Int? = null,

    val supplementId: Long,
    val scheduledTime: LocalTime?,         // null if this was "Log now/extra"
    val amountText: String?,               // optional display like "1.0"
    val unitText: String?                  // optional display like "capsule"
) : TimelineItemUiModel {

    override val rowType: TodayUiRowType = TodayUiRowType.SUPPLEMENT_DOSE_LOG

    override val key: String =
        "SUPPLEMENT_DOSE_LOG-$supplementId-$time-$id"
}




















//sealed interface TimelineItemUiModel {
//
//    /** Stable UI key for LazyColumn */
//    val key: String
//
//    /** Source entity ID (supplementId, mealId, activityId) */
//    val id: Long
//
//    /** Time this item occurs */
//    val time: LocalTime
//
//    /** Primary display text */
//    val title: String
//
//    /** Optional secondary text */
//    val subtitle: String?
//
//    /** Row category for styling & behavior */
//    val rowType: TodayUiRowType
//
//    data class Supplement(
//        override val id: Long,
//        override val time: LocalTime,
//        override val title: String,
//        override val subtitle: String?,
//        val doseState: MealAwareDoseState?,
//        val suggestedDose: Double,
//        val defaultUnit: SupplementDoseUnit
//    ) : TimelineItemUiModel {
//        override val rowType = TodayUiRowType.SUPPLEMENT
//        override val key = "${rowType.name}-$id-$time"
//    }
//
//    data class Meal(
//        override val id: Long,
//        override val time: LocalTime,
//        override val title: String,
//        override val subtitle: String?,
//        val type: MealType
//    ) : TimelineItemUiModel {
//        override val rowType = TodayUiRowType.MEAL
//        override val key = "${rowType.name}-$id-$time"
//    }
//
//    data class Activity(
//        override val id: Long,
//        override val time: LocalTime,
//        override val title: String,
//        override val subtitle: String?,
//        val activityType: ActivityType,
//        val endTime: LocalTime?
//    ) : TimelineItemUiModel {
//        override val rowType = TodayUiRowType.ACTIVITY
//        override val key = "${rowType.name}-$id-$time"
//    }
//}
