package com.example.hastanghubaga.feature.today

import androidx.compose.runtime.Composable
import com.example.hastanghubaga.ui.timeline.TimelineItemUiModel

object TodayScreenContract {

    /**
     * UI State – single source of truth
     */
    data class State(
        val isLoading: Boolean = false,
        val timelineItems: List<TimelineItemUiModel> = emptyList(),
        val errorMessage: String? = null
    )

    /**
     * User / system intents
     */
    sealed interface Intent {
        data object LoadToday : Intent
        data object Refresh : Intent
        data class TimelineItemClicked(val item: TimelineItemUiModel) : Intent
    }

    /**
     * One-off effects (not state)
     */
    sealed interface Effect {
        data class ShowSnackbar(
            val message: String
        ) : Effect
        data class ShowBanner(
            val message: String
        ) : Effect
        data class ShowBottomSheet(
            val content: @Composable () -> Unit
        ) : Effect
        data class ShowError(
            val message: String
        ) : Effect
        data class Navigate(
            val destination: Destination
        ) : Effect
    }


    sealed interface Destination {
        data class Supplement(val id: Long) : Destination
        data class Meal(val id: Long) : Destination
        data class Activity(val id: Long) : Destination
    }
}
