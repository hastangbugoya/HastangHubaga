package com.example.hastanghubaga.domain.usecase.todaytimeline

import android.util.Log
import com.example.hastanghubaga.data.local.entity.supplement.SupplementDoseUnit
import com.example.hastanghubaga.domain.repository.supplement.SupplementRepository
import com.example.hastanghubaga.ui.timeline.TimelineItemUiModel
import javax.inject.Inject
/**
 * Resolves the **domain meaning** of a tap on a Today timeline item.
 *
 * This use case performs **pure decision logic only**:
 * it inspects a [TimelineItemUiModel] and determines what *action*,
 * if any, should result from the tap.
 *
 * ## Why `resolve()` instead of `invoke()`
 * This use case does **not**:
 * - Mutate state
 * - Access repositories
 * - Perform I/O
 * - Launch coroutines
 *
 * Instead, it answers the question:
 *
 * > “Given this timeline item, what should happen next?”
 *
 * Using `resolve()` makes this intent explicit and communicates that:
 * - The operation is side-effect free
 * - The result is deterministic
 * - The method is safe to call synchronously
 *
 * ## Role in the Architecture
 * This use case sits between:
 *
 * - **UI intent** (a tap gesture)
 * - **UI effects / commands** (dialogs, logging, navigation)
 *
 * It decouples the ViewModel from knowledge of:
 * - Timeline item types
 * - Feature-specific branching logic
 * - Future interaction rules
 *
 * The ViewModel simply interprets the returned [TimelineTapAction].
 *
 * ## Responsibilities
 * - Identify the type of timeline item tapped
 * - Extract stable domain identifiers (IDs, defaults)
 * - Return an explicit [TimelineTapAction]
 *
 * ## Non-Responsibilities
 * - No validation
 * - No persistence
 * - No UI logic
 * - No time or clock decisions
 *
 * ## Extensibility
 * New timeline item types or behaviors should be added by:
 * 1. Introducing a new [TimelineTapAction] variant
 * 2. Extending the `when` expression in [resolve]
 *
 * This preserves exhaustive handling and compile-time safety.
 */
class HandleTimelineItemTapUseCase @Inject constructor(
    private val supplementLogRepository: SupplementRepository
) {
    /**
     * Resolves the appropriate [TimelineTapAction] for a tapped
     * [TimelineItemUiModel].
     *
     * @param item
     * The UI model representing a single entry in the Today timeline.
     * This model is treated as read-only input.
     *
     * @return
     * A [TimelineTapAction] describing the domain-level outcome of the tap.
     * The result is never `null`; `NoOp` is used when no action is required.
     */
    fun resolve(item: TimelineItemUiModel): TimelineTapAction {
        return when (item) {
            is TimelineItemUiModel.Supplement ->
                TimelineTapAction.RequestDoseInput(
                    supplementId = item.id,
                    defaultUnit = item.defaultUnit,
                    suggestedDose = item.suggestedDose,
                    title = item.title,
                    scheduledTime = item.time
                )

            else ->
                TimelineTapAction.NoOp
        }
    }

    private suspend fun logSupplement(item: TimelineItemUiModel.Supplement) {
        Log.d(
            "TimelineTap",
            "Logging supplement intake → supplementId=${item.id}"
        )
    }
}
