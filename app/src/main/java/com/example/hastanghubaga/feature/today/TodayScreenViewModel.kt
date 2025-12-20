package com.example.hastanghubaga.feature.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hastanghubaga.domain.model.timeline.LogDoseInput
import com.example.hastanghubaga.domain.model.timeline.TimelineItem
import com.example.hastanghubaga.domain.time.TimePolicy
import com.example.hastanghubaga.domain.time.TimeUseIntent
import com.example.hastanghubaga.domain.usecase.activity.GetActivitiesForDateUseCase
import com.example.hastanghubaga.domain.usecase.meal.GetMealsForDateUseCase
import com.example.hastanghubaga.domain.usecase.supplement.GetSupplementsWithUserSettingsForDateUseCase
import com.example.hastanghubaga.domain.usecase.todaytimeline.BuildTodayTimelineUseCase
import com.example.hastanghubaga.domain.usecase.todaytimeline.HandleTimelineItemTapUseCase
import com.example.hastanghubaga.domain.usecase.todaytimeline.LogSupplementDoseUseCase
import com.example.hastanghubaga.domain.usecase.todaytimeline.TimelineTapAction
import com.example.hastanghubaga.ui.timeline.TimelineItemUiModel
import com.example.hastanghubaga.ui.timeline.TodayUiRowType
import com.example.hastanghubaga.ui.timeline.toTimelineItemUiModels
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class TodayScreenViewModel @Inject constructor(
    private val getSupplementsForDate: GetSupplementsWithUserSettingsForDateUseCase,
    private val getMealsForDate: GetMealsForDateUseCase, // can be stubbed
    private val getActivitiesForDate: GetActivitiesForDateUseCase,
    private val buildTodayTimeline: BuildTodayTimelineUseCase,
    private val handleTimelineItemTapUseCase: HandleTimelineItemTapUseCase,
    private val logSupplementDoseUseCase: LogSupplementDoseUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(TodayScreenContract.State())
    val state: StateFlow<TodayScreenContract.State> = _state

    private val _effect = Channel<TodayScreenContract.Effect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private var loadJob: Job? = null

    fun onIntent(intent: TodayScreenContract.Intent) {
        when (intent) {
            is TodayScreenContract.Intent.LoadToday ->
                loadToday(LocalDate.now())

            is TodayScreenContract.Intent.Refresh ->
                loadToday(LocalDate.now())

            is TodayScreenContract.Intent.TimelineItemClicked -> {
                handleTimelineItemClicked(intent.item)
            }
            is TodayScreenContract.Intent.ConfirmDose -> {
                viewModelScope.launch {
                    val timeUseIntent =
                    when {
                        intent.actualTime != null ->
                            TimeUseIntent.Explicit(
                                date = TimePolicy.todayLocal(),
                                time = intent.actualTime
                            )

                        else ->
                            TimeUseIntent.Scheduled(intent.scheduledTime)
                    }
                    logSupplementDoseUseCase(
                        LogDoseInput(
                            supplementId = intent.supplementId,
                            fractionTaken = intent.amount,
                            unit = intent.unit,
                            timeUseIntent = timeUseIntent
                        )
                    )
                }
            }
        }
    }

    fun loadToday(date: LocalDate = TimePolicy.todayLocal()) {
        loadJob?.cancel()

        loadJob = viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                combine(
                    getSupplementsForDate(date),
                    getMealsForDate(date),       // can return emptyList() for now
                    getActivitiesForDate(date)
                ) { supplements, meals, activities ->

                    buildTodayTimeline(
                        supplements = supplements,
                        meals = meals,
                        activities = activities
                    )
                }
                    .collectLatest { timeline ->
                        _state.update {
                            it.copy(
                                isLoading = false,
                                domainTimelineItems = timeline,
                                uiTimelineItems = timeline.toTimelineItemUiModels()
                            )
                        }
                    }

            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Failed to load timeline"
                    )
                }
            }
        }
    }

    private fun List<TimelineItem>.collectLatest(function: Any) {}

    private fun handleTimelineItemClicked(
        uiItem: TimelineItemUiModel
    ) {
        val domainItem = findDomainItemFor(uiItem) ?: return
        when (
            val action = handleTimelineItemTapUseCase.resolve(uiItem)
        ) {
            is TimelineTapAction.RequestDoseInput -> {
                viewModelScope.launch {
                    _effect.send(
                        TodayScreenContract.Effect.ShowDoseInputDialog(
                            supplementId = action.supplementId,
                            title = action.title,
                            defaultUnit = action.defaultUnit,
                            suggestedDose = action.suggestedDose,
                            scheduledTime = action.scheduledTime
                        )
                    )
                }
            }

            is TimelineTapAction.ActivityTapped -> {
                // future
            }

            is TimelineTapAction.MealTapped -> {
                // future
            }

            TimelineTapAction.NoOp -> Unit
        }
    }

    private data class TimelineIdentity(
        val type: TodayUiRowType,
        val id: Long,
        val time: LocalTime
    )

    private fun TimelineItemUiModel.identity(): TimelineIdentity =
        TimelineIdentity(
            type = rowType,
            id = id,
            time = time
        )

    private fun findDomainItemFor(
        uiItem: TimelineItemUiModel
    ): TimelineItem? {
        val identity = uiItem.identity()
        return state.value.domainTimelineItems.firstOrNull { domainItem ->
            when {
                identity.type == TodayUiRowType.SUPPLEMENT &&
                        domainItem is TimelineItem.SupplementTimelineItem ->
                    domainItem.supplement.supplement.id == identity.id &&
                            domainItem.time == identity.time

                identity.type == TodayUiRowType.ACTIVITY &&
                        domainItem is TimelineItem.ActivityTimelineItem ->
                    domainItem.activity.id == identity.id &&
                            domainItem.time == identity.time

                identity.type == TodayUiRowType.MEAL &&
                        domainItem is TimelineItem.MealTimelineItem ->
                    domainItem.meal.id == identity.id &&
                            domainItem.time == identity.time

                else -> false
            }
        }
    }
}
