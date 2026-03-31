package com.example.hastanghubaga.domain.usecase.todaytimeline

import com.example.hastanghubaga.data.local.entity.supplement.SupplementDoseUnit
import kotlinx.datetime.LocalTime

/**
 * Represents the **domain-level outcome** of tapping a timeline item
 * on the Today screen.
 *
 * This sealed interface is the output of a *decision-only* use case
 * (e.g. `HandleTimelineItemTapUseCase.resolve(...)`).
 *
 * ## Purpose
 * - Decouple **UI gestures** from **business decisions**
 * - Allow the domain layer to express *intent* without knowing about UI
 * - Enable multi-step flows (dialogs, confirmations) in a controlled way
 *
 * ## Architectural Rules
 * - Implementations MUST be pure data (no logic)
 * - No Android, Compose, or Room dependencies
 * - No side effects or persistence
 *
 * The ViewModel interprets these actions and translates them into UI
 * effects or follow-up use cases.
 */
sealed interface TimelineTapAction {

    /**
     * Indicates that tapping a supplement timeline item requires
     * explicit user input before the intake can be logged.
     *
     * This action is typically translated by the ViewModel into a UI
     * effect such as showing a dose input dialog.
     *
     * ## Occurrence-aware logging
     * A supplement timeline item may represent a **concrete planned occurrence**
     * (e.g. "Creatine at 8:15 AM").
     *
     * When available, [occurrenceId] links this action to that specific
     * planned occurrence so the eventual log can be reconciled 1:1 with
     * the planner/timeline item.
     *
     * If null:
     * - the action may represent an ad-hoc / extra dose
     * - or the system is still operating in a log-first mode
     *
     * @property supplementId
     * The stable identifier of the supplement being logged.
     * This ID must be sufficient for downstream use cases to persist
     * an intake log without relying on UI models.
     *
     * @property title
     * Display name of the supplement. Used for UI presentation only.
     *
     * @property scheduledTime
     * The time associated with the planned occurrence.
     * Used as a UI hint and may be used as a fallback planned time.
     *
     * @property defaultUnit
     * The preferred or most recently used dose unit for this supplement.
     * Used only as a UI default and does not imply validation.
     *
     * @property suggestedDose
     * A suggested dose amount (e.g., from user settings or schedule).
     * This value is advisory only and must be validated later.
     *
     * @property occurrenceId
     * Optional stable identifier for the specific planned occurrence.
     * Enables one-to-one reconciliation between planner items and logs.
     */
    data class RequestDoseInput(
        val supplementId: Long,
        val title: String,
        val scheduledTime: LocalTime,
        val defaultUnit: SupplementDoseUnit,
        val suggestedDose: Double,
        val occurrenceId: String? = null
    ) : TimelineTapAction

    /**
     * Indicates that the tapped timeline item represents an Activity.
     *
     * This variant exists for completeness and future expansion.
     * At present, it is intentionally handled as a no-op.
     *
     * Future implementations may:
     * - Prompt for duration or intensity
     * - Navigate to an activity detail screen
     * - Log a completed activity session
     *
     * @property activityId
     * The stable identifier of the activity definition or scheduled item.
     */
    data class ActivityTapped(
        val activityId: Long
    ) : TimelineTapAction

    /**
     * Indicates that the tapped timeline item represents a Meal.
     *
     * This variant exists to reserve a clear decision boundary for
     * meal-related interactions.
     *
     * Future implementations may:
     * - Navigate to meal details
     * - Prompt for portion confirmation
     * - Log meal consumption
     *
     * @property mealId
     * The stable identifier of the meal or planned meal entry.
     */
    data class MealTapped(
        val mealId: Long
    ) : TimelineTapAction

    /**
     * Indicates that no action should be taken for the tap.
     *
     * This is a deliberate, explicit outcome rather than `null`,
     * ensuring the decision space is fully modeled and exhaustively
     * handled by the ViewModel.
     *
     * Common reasons for this action include:
     * - Taps on non-interactive timeline items
     * - Features not yet implemented
     * - Read-only informational entries
     */
    object NoOp : TimelineTapAction
}