package com.example.hastanghubaga.feature.today

import androidx.compose.runtime.Composable
import com.example.hastanghubaga.data.local.entity.supplement.SupplementDoseUnit
import com.example.hastanghubaga.domain.model.timeline.TimelineItem
import com.example.hastanghubaga.ui.timeline.TimelineItemUiModel
import java.time.LocalTime

object TodayScreenContract {

    /**
     * UI State – single source of truth
     */
    data class State(
        val isLoading: Boolean = false,
        val uiTimelineItems: List<TimelineItemUiModel> = emptyList(),
        val domainTimelineItems: List<TimelineItem> = emptyList(),
        val errorMessage: String? = null
    )

    /**
     * User / system intents
     */
    sealed interface Intent {
        data object LoadToday : Intent
        data object Refresh : Intent
        data class TimelineItemClicked(val item: TimelineItemUiModel) : Intent

        data class ConfirmDose(
            val supplementId: Long,
            val amount: Double,
            val unit: SupplementDoseUnit,
            val scheduledTime: LocalTime,
            val actualTime: LocalTime?
        ) : Intent
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
//        data class ShowTimelineItemInfo(
//            val title: String,
//            val subtitle: String,
//            val time: String,
//            val key: String
//        ) : Effect

        data class ShowDoseInputDialog(
            val supplementId: Long,
            val title: String,
            val scheduledTime: LocalTime,
            val defaultUnit: SupplementDoseUnit,
            val suggestedDose: Double,
        ) : Effect
    }


    sealed interface Destination {
        data class Supplement(val id: Long) : Destination
        data class Meal(val id: Long) : Destination
        data class Activity(val id: Long) : Destination
    }
}
