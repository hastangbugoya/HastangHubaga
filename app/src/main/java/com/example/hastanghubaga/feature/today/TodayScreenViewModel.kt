package com.example.hastanghubaga.feature.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hastanghubaga.domain.model.timeline.TimelineItem
import com.example.hastanghubaga.domain.usecase.activity.GetActivitiesForDateUseCase
import com.example.hastanghubaga.domain.usecase.meal.GetMealsForDateUseCase
import com.example.hastanghubaga.domain.usecase.supplement.GetSupplementsWithUserSettingsForDateUseCase
import com.example.hastanghubaga.domain.usecase.todaytimeline.BuildTodayTimelineUseCase
import com.example.hastanghubaga.ui.timeline.TimelineItemUiModel
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
import javax.inject.Inject

@HiltViewModel
class TodayScreenViewModel @Inject constructor(
    private val getSupplementsForDate: GetSupplementsWithUserSettingsForDateUseCase,
    private val getMealsForDate: GetMealsForDateUseCase, // can be stubbed
    private val getActivitiesForDate: GetActivitiesForDateUseCase,
    private val buildTodayTimeline: BuildTodayTimelineUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(TodayScreenContract.State())
    val state: StateFlow<TodayScreenContract.State> = _state

    private val _effect = Channel<TodayScreenContract.Effect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private var loadJob: Job? = null

    fun onIntent(intent: TodayScreenContract.Intent) {
        when (intent) {
            TodayScreenContract.Intent.LoadToday ->
                loadToday(LocalDate.now())

            TodayScreenContract.Intent.Refresh ->
                loadToday(LocalDate.now())

            is TodayScreenContract.Intent.TimelineItemClicked -> {
                viewModelScope.launch {
                    _effect.send(
                        TodayScreenContract.Effect.ShowTimelineItemInfo(
                            title = intent.item.title,
                            subtitle = intent.item.subtitle.orEmpty(),
                            time = intent.item.time.toString(),
                            key = intent.item.key
                        )
                    )
                }
            }
        }
    }

    fun loadToday(date: LocalDate = LocalDate.now()) {
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
                                timelineItems = timeline.toTimelineItemUiModels()
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

    private fun handleItemClick(item: TimelineItemUiModel) {
        val destination =
            when (item) {
                is TimelineItemUiModel.Supplement ->
                    TodayScreenContract.Destination.Supplement(item.id)

                is TimelineItemUiModel.Meal ->
                    TodayScreenContract.Destination.Meal(item.id)

                is TimelineItemUiModel.Activity ->
                    TodayScreenContract.Destination.Activity(item.id)
            }

        viewModelScope.launch {
            _effect.send(
                TodayScreenContract.Effect.Navigate(destination)
            )
        }
    }
}
