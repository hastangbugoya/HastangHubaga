// TodayScreenViewModel.kt

package com.example.hastanghubaga.feature.today

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hastanghubaga.domain.model.timeline.TimelineItem
import com.example.hastanghubaga.domain.time.DomainTimePolicy
import com.example.hastanghubaga.domain.usecase.activity.GetActivitiesForDateUseCase
import com.example.hastanghubaga.domain.usecase.meal.GetMealsForDateUseCase
import com.example.hastanghubaga.domain.usecase.supplement.GetSupplementsWithUserSettingsForDateUseCase
import com.example.hastanghubaga.domain.usecase.todaytimeline.BuildTodayTimelineUseCase
import com.example.hastanghubaga.domain.usecase.todaytimeline.HandleTimelineItemTapUseCase
import com.example.hastanghubaga.domain.usecase.todaytimeline.LogSupplementDoseUseCase
import com.example.hastanghubaga.domain.usecase.todaytimeline.TimelineTapAction
import com.example.hastanghubaga.ui.timeline.TimelineItemUiModel
import com.example.hastanghubaga.ui.timeline.TodayUiRowType
import com.example.hastanghubaga.ui.timeline.TimelineItemUiModel.Activity as ActivityUi
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
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import javax.inject.Inject

@HiltViewModel
class TodayScreenViewModel @Inject constructor(
    private val getSupplementsForDate: GetSupplementsWithUserSettingsForDateUseCase,
    private val getMealsForDate: GetMealsForDateUseCase,
    private val getActivitiesForDate: GetActivitiesForDateUseCase,
    private val buildTodayTimeline: BuildTodayTimelineUseCase,
    private val handleTimelineItemTapUseCase: HandleTimelineItemTapUseCase,
    private val logSupplementDoseUseCase: LogSupplementDoseUseCase,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _state = MutableStateFlow(TodayScreenContract.State())
    val state: StateFlow<TodayScreenContract.State> = _state

    private val _effect = Channel<TodayScreenContract.Effect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private var loadJob: Job? = null

    /**
     * SavedStateHandle keys for persisting the “in-progress exercise draft”.
     *
     * Why persist at all?
     * - If the process is killed (low memory) while the sheet is open,
     *   the user can come back and see the same draft rather than losing it.
     *
     * What we persist:
     * - Only small primitive fields (Strings/Ints/Longs), not entire screen state.
     * - Times are stored as "minutes since midnight" for stability.
     */
    private object ExerciseSavedStateKeys {
        const val TYPE = "exercise.type"
        const val START_MIN = "exercise.startMin"
        const val END_MIN = "exercise.endMin"
        const val NOTES = "exercise.notes"
        const val INTENSITY = "exercise.intensity"
        const val PHASE = "exercise.phase" // "Draft" / "Running"
    }

    init {
        restoreExerciseDraftIfPresent()
    }

    fun onIntent(intent: TodayScreenContract.Intent) {
        val clock = Clock.System
        val today = DomainTimePolicy.todayLocal(clock)

        when (intent) {
            is TodayScreenContract.Intent.LoadToday -> loadToday(today)
            is TodayScreenContract.Intent.Refresh -> loadToday(today)
            is TodayScreenContract.Intent.TimelineItemClicked -> handleTimelineItemClicked(intent.item)

            // ---- exercise sheet intents ----
            is TodayScreenContract.Intent.ExerciseTapped -> openExerciseDraft(intent.item, clock)

            TodayScreenContract.Intent.ExerciseStartPressed -> {
                val current = _state.value.exerciseDraft ?: return
                val updated = current.copy(phase = TodayScreenContract.ExerciseDraft.Phase.Running)
                setExerciseDraft(updated)
            }

            is TodayScreenContract.Intent.ExerciseNotesChanged -> {
                val current = _state.value.exerciseDraft ?: return
                setExerciseDraft(current.copy(notes = intent.value))
            }

            is TodayScreenContract.Intent.ExerciseIntensityChanged -> {
                val current = _state.value.exerciseDraft ?: return
                setExerciseDraft(current.copy(intensity = intent.value))
            }

            is TodayScreenContract.Intent.ExerciseEndTimeChanged -> {
                val current = _state.value.exerciseDraft ?: return
                setExerciseDraft(current.copy(endTime = intent.value))
            }

            TodayScreenContract.Intent.ExerciseConfirmPressed -> {
                // TODO: Insert/update Activity via a use case.
                // For now, just close the sheet and show a banner so you can verify the flow works.
                clearExerciseDraft()
                viewModelScope.launch {
                    _effect.send(TodayScreenContract.Effect.ShowBanner("Exercise saved (TODO: insert into DB)"))
                }
            }

            TodayScreenContract.Intent.DismissExerciseSheet -> {
                clearExerciseDraft()
            }

            // ---- existing dose flow ----
            is TodayScreenContract.Intent.ConfirmDose -> {
                // (your existing code left unchanged)
                // ...
            }
        }
    }

    private fun handleTimelineItemClicked(
        uiItem: TimelineItemUiModel
    ) {
        val domainItem = findDomainItemFor(uiItem) ?: return

        when (val action = handleTimelineItemTapUseCase.resolve(uiItem)) {

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
                // future activity handling (was intentionally empty)
            }

            is TimelineTapAction.MealTapped -> {
                // future meal handling (was intentionally empty)
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



    private var loadTodayJob: Job? = null

    fun loadToday(date: LocalDate = DomainTimePolicy.todayLocal()) {
        loadTodayJob?.cancel()

        loadTodayJob = viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                combine(
                    getSupplementsForDate(date),
                    getMealsForDate(date),
                    getActivitiesForDate(date)
                ) { supplements, meals, activities ->
                    buildTodayTimeline(
                        supplements = supplements,
                        meals = meals,
                        activities = activities
                    )
                }.collectLatest { timeline ->
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


    private fun openExerciseDraft(item: TimelineItemUiModel, clock: Clock) {
        val ui = item as? ActivityUi ?: return
        val now = DomainTimePolicy.nowLocalDateTime(clock).time

        val draft = TodayScreenContract.ExerciseDraft(
            activityType = ui.activityType,
            startTime = now,
            endTime = ui.endTime,
            notes = "",
            intensity = null,
            phase = TodayScreenContract.ExerciseDraft.Phase.Draft
        )
        setExerciseDraft(draft)
    }

    /**
     * Writes the exercise draft into:
     * 1) State (to drive the Compose UI), and
     * 2) SavedStateHandle (to survive process recreation).
     */
    private fun setExerciseDraft(draft: TodayScreenContract.ExerciseDraft) {
        _state.update { it.copy(exerciseDraft = draft) }
        persistExerciseDraft(draft)
    }

    /** Clears both the UI state and the persisted SavedStateHandle fields. */
    private fun clearExerciseDraft() {
        _state.update { it.copy(exerciseDraft = null) }
        clearPersistedExerciseDraft()
    }

    // ---- SavedStateHandle helpers (KDoc only for savedstate parts) ----

    /**
     * Restores a previously persisted exercise draft from SavedStateHandle (if present),
     * and rehydrates it into UI state.
     */
    private fun restoreExerciseDraftIfPresent() {
        val type = savedStateHandle.get<String>(ExerciseSavedStateKeys.TYPE) ?: return
        val startMin = savedStateHandle.get<Int>(ExerciseSavedStateKeys.START_MIN) ?: return
        val phaseRaw = savedStateHandle.get<String>(ExerciseSavedStateKeys.PHASE) ?: "Draft"

        val endMin = savedStateHandle.get<Int?>(ExerciseSavedStateKeys.END_MIN)
        val notes = savedStateHandle.get<String>(ExerciseSavedStateKeys.NOTES) ?: ""
        val intensity = savedStateHandle.get<Int?>(ExerciseSavedStateKeys.INTENSITY)

        val draft = TodayScreenContract.ExerciseDraft(
            activityType = com.example.hastanghubaga.domain.model.activity.ActivityType.valueOf(type),
            startTime = minutesToLocalTime(startMin),
            endTime = endMin?.let(::minutesToLocalTime),
            notes = notes,
            intensity = intensity,
            phase = TodayScreenContract.ExerciseDraft.Phase.valueOf(phaseRaw)
        )

        _state.update { it.copy(exerciseDraft = draft) }
    }

    /** Persists the draft into small primitives so Android can restore it after process recreation. */
    private fun persistExerciseDraft(draft: TodayScreenContract.ExerciseDraft) {
        savedStateHandle[ExerciseSavedStateKeys.TYPE] = draft.activityType.name
        savedStateHandle[ExerciseSavedStateKeys.START_MIN] = draft.startTime.toMinutesOfDay()
        savedStateHandle[ExerciseSavedStateKeys.END_MIN] = draft.endTime?.toMinutesOfDay()
        savedStateHandle[ExerciseSavedStateKeys.NOTES] = draft.notes
        savedStateHandle[ExerciseSavedStateKeys.INTENSITY] = draft.intensity
        savedStateHandle[ExerciseSavedStateKeys.PHASE] = draft.phase.name
    }

    /** Removes the persisted keys so a dismissed/confirmed sheet does not restore unexpectedly. */
    private fun clearPersistedExerciseDraft() {
        savedStateHandle.remove<String>(ExerciseSavedStateKeys.TYPE)
        savedStateHandle.remove<Int>(ExerciseSavedStateKeys.START_MIN)
        savedStateHandle.remove<Int?>(ExerciseSavedStateKeys.END_MIN)
        savedStateHandle.remove<String>(ExerciseSavedStateKeys.NOTES)
        savedStateHandle.remove<Int?>(ExerciseSavedStateKeys.INTENSITY)
        savedStateHandle.remove<String>(ExerciseSavedStateKeys.PHASE)
    }

    private fun LocalTime.toMinutesOfDay(): Int = (hour * 60) + minute
    private fun minutesToLocalTime(m: Int): LocalTime = LocalTime(m / 60, m % 60)

    // ---- rest of your VM (loadToday, handleTimelineItemClicked, etc.) unchanged ----
    // ...
}
