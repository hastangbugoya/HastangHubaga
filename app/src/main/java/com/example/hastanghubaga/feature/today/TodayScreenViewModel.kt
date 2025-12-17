package com.example.hastanghubaga.feature.today

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hastanghubaga.domain.usecase.activity.GetActivitiesForDateUseCase
import com.example.hastanghubaga.domain.usecase.meal.GetMealsForDateUseCase
import com.example.hastanghubaga.domain.usecase.supplement.GetSupplementsWithUserSettingsForDateUseCase
import com.example.hastanghubaga.domain.usecase.todaytimeline.BuildTodayTimelineUseCase
import com.example.hastanghubaga.ui.timeline.toTimelineItemUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/**
 * ViewModel for the Today screen.
 *
 * Responsibilities:
 * - Observe domain data for the current day (supplements, meals, activities)
 * - Delegate timeline construction to [BuildTodayTimelineUseCase]
 * - Expose a single immutable UI state to Compose
 *
 * Non-responsibilities:
 * - Does NOT sort, merge, or interpret timeline items
 * - Does NOT map domain models directly to UI rows
 */
@HiltViewModel
class TodayScreenViewModel @Inject constructor(
    private val getSupplementsWithUserSettingsForDateUseCase: GetSupplementsWithUserSettingsForDateUseCase,
    private val getMealsForDateUseCase: GetMealsForDateUseCase,
    private val getActivitiesForDateUseCase: GetActivitiesForDateUseCase,
    private val buildTodayTimelineUseCase: BuildTodayTimelineUseCase
) : ViewModel() {

    private var todayJob: Job? = null

    private val _state = MutableStateFlow(TodayScreenState())
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<TodayScreenEvent>()
    val events = _events.asSharedFlow()


    init {
//        runBlocking {
//            getSupplementsWithUserSettingsForDateUseCase(LocalDate.now()).collectLatest { it ->
//                it.forEach { item ->
//                    Log.e("Meow","Seeing is believing ${item.supplement.name} ${item.scheduledTimes}")
//                }
//            }
//        }
        loadToday()
    }

    /**
     * Loads and observes all "today" domain data and produces timeline items.
     *
     * This function:
     * - Cancels any existing collectors
     * - Combines supplements, meals, and activities
     * - Delegates ordering and merging to the domain use case
     */
    fun loadToday() {
        todayJob?.cancel()
        todayJob =
            viewModelScope.launch {
                val today = LocalDate.now()

                _state.update { it.copy(isLoading = true) }

                combine(
                    getSupplementsWithUserSettingsForDateUseCase(today),
                    getMealsForDateUseCase(today),
                    getActivitiesForDateUseCase(today)
                ) { supplements, meals, activities ->
                    buildTodayTimelineUseCase(
                        supplements = supplements,
                        meals = meals,
                        activities = activities
                    )
                }.map { timelineItems ->
                    timelineItems.map { it.toTimelineItemUiModel() }
                }
                    .collectLatest { uiItems ->
                        _state.update {
                            it.copy(
                                isLoading = false,
                                timelineItems = uiItems
                            )
                        }
                    }
                showBottomSheet ({"Timeline loaded"})
            }
    }

    sealed class TodayScreenEvent {
        data class ShowBanner(val message: String, val isError: Boolean = false) : TodayScreenEvent()
        data class ShowBottomSheet(val content: @Composable () -> Unit) : TodayScreenEvent()
        data class ShowSnackbar(val message: String) : TodayScreenEvent()
    }

    private suspend fun showBanner(message: String, isError: Boolean = false) {
        _events.emit(TodayScreenEvent.ShowBanner(message, isError))
    }

    private suspend fun showBottomSheet(content: @Composable () -> Unit) {
        _events.emit(TodayScreenEvent.ShowBottomSheet(content))
    }

    private suspend fun showSnackbar(message: String) {
        _events.emit(TodayScreenEvent.ShowSnackbar(message))
    }

}
