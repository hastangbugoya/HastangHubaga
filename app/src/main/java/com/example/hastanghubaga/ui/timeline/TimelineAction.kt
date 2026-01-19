package com.example.hastanghubaga.ui.timeline

import com.example.hastanghubaga.ui.timeline.TimelineAction.*

/**
 * Represents user interactions with timeline rows.
 *
 * These are UI intents, not navigation instructions.
 */
sealed interface TimelineAction {

    data class OpenSupplement(
        val supplementId: Long
    ) : TimelineAction

    data class OpenMeal(
        val mealId: Long
    ) : TimelineAction

    data class OpenActivity(
        val activityId: Long
    ) : TimelineAction

    data object NoOp : TimelineAction
}


/**
 * Maps a timeline item to its default click action.
 */
fun TimelineItemUiModel.toClickAction(): TimelineAction =
    when (this) {
        is SupplementUiModel ->
            OpenSupplement(id)

        is MealUiModel ->
            OpenMeal(id)

        is ActivityUiModel ->
            OpenActivity(id)

        is TimelineItem.SupplementDoseLog -> NoOp
    }
