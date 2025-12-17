package com.example.hastanghubaga.ui.timeline
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
}


/**
 * Maps a timeline item to its default click action.
 */
fun TimelineItemUiModel.toClickAction(): TimelineAction =
    when (this) {
        is TimelineItemUiModel.Supplement ->
            TimelineAction.OpenSupplement(id)

        is TimelineItemUiModel.Meal ->
            TimelineAction.OpenMeal(id)

        is TimelineItemUiModel.Activity ->
            TimelineAction.OpenActivity(id)
    }
