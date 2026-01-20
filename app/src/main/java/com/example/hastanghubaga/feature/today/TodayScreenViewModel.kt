// TodayScreenViewModel.kt

package com.example.hastanghubaga.feature.today

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hastanghubaga.domain.model.activity.ActivityType
import com.example.hastanghubaga.domain.model.meal.LogMealInput
import com.example.hastanghubaga.domain.model.meal.NutritionInput
import com.example.hastanghubaga.domain.model.timeline.LogDoseInput
import com.example.hastanghubaga.domain.time.DomainTimePolicy
import com.example.hastanghubaga.domain.time.TimeUseIntent
import com.example.hastanghubaga.domain.time.TimeUseIntent.*
import com.example.hastanghubaga.domain.usecase.activity.GetActivitiesForDateUseCase
import com.example.hastanghubaga.domain.usecase.activity.SaveExerciseActivityUseCase
import com.example.hastanghubaga.domain.usecase.meal.GetMealsForDateUseCase
import com.example.hastanghubaga.domain.usecase.meal.LogMealUseCase
import com.example.hastanghubaga.domain.usecase.supplement.GetSupplementsWithUserSettingsForDateUseCase
import com.example.hastanghubaga.domain.usecase.todaytimeline.BuildTodayTimelineUseCase
import com.example.hastanghubaga.domain.usecase.todaytimeline.HandleTimelineItemTapUseCase
import com.example.hastanghubaga.domain.usecase.todaytimeline.LogSupplementDoseUseCase
import com.example.hastanghubaga.domain.usecase.todaytimeline.TimelineTapAction
import com.example.hastanghubaga.feature.today.TodayScreenContract.Effect.*
import com.example.hastanghubaga.feature.today.TodayScreenContract.ExerciseDraft
import com.example.hastanghubaga.ui.timeline.ActivityUiModel
import com.example.hastanghubaga.ui.timeline.TimelineItem
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
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
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
    private val savedExerciseActivityUseCase: SaveExerciseActivityUseCase,
    private val logMealUseCase: LogMealUseCase
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
            is TodayScreenContract.Intent.LoadToday ->
                loadToday(today)

            is TodayScreenContract.Intent.Refresh ->
                loadToday(today)

            is TodayScreenContract.Intent.TimelineItemClicked -> {
                handleTimelineItemClicked(intent.item)
            }

            is TodayScreenContract.Intent.ConfirmDose -> {
                viewModelScope.launch {
                    val timeUseIntent =
                        when {
                            intent.actualTime != null -> Explicit(today, intent.actualTime)
                            intent.scheduledTime != null -> Scheduled(intent.scheduledTime)
                            else -> ActualNow
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

            is TodayScreenContract.Intent.ExerciseTapped -> {
                val activityUi = intent.item as? ActivityUiModel ?: return
                val start = nowLocalTime(clock)

                _state.update {
                    it.copy(
                        exerciseDraft = ExerciseDraft(
                            activityType = activityUi.activityType,
                            startTime = start,
                            endTime = null,
                            notes = "",
                            intensity = null,
                            phase = ExerciseDraft.Phase.Draft
                        )
                    )
                }
            }

            TodayScreenContract.Intent.ExerciseStartPressed -> {
                val existing = state.value.exerciseDraft ?: return
                if (existing.phase == ExerciseDraft.Phase.Running) return

                // startTime is already prefilled, but keep it “now” if you want it to refresh on Start.
                val start = existing.startTime

                _state.update {
                    it.copy(
                        exerciseDraft = existing.copy(
                            startTime = start,
                            endTime = null,
                            phase = ExerciseDraft.Phase.Running
                        )
                    )
                }
            }

            is TodayScreenContract.Intent.ExerciseNotesChanged -> {
                val existing = state.value.exerciseDraft ?: return
                _state.update { it.copy(exerciseDraft = existing.copy(notes = intent.value)) }
            }

            is TodayScreenContract.Intent.ExerciseIntensityChanged -> {
                val existing = state.value.exerciseDraft ?: return
                _state.update { it.copy(exerciseDraft = existing.copy(intensity = intent.value)) }
            }

            is TodayScreenContract.Intent.ExerciseEndTimeChanged -> {
                val existing = state.value.exerciseDraft ?: return
                // Optional manual edit support; keep it clamped if provided.
                val clamped =
                    intent.value?.let { clampEndTimeNotBeforeStart(existing.startTime, it) }
                _state.update { it.copy(exerciseDraft = existing.copy(endTime = clamped)) }
            }

            TodayScreenContract.Intent.ExerciseConfirmPressed -> {
                val draft = state.value.exerciseDraft ?: return
                if (draft.phase != ExerciseDraft.Phase.Running) return

                viewModelScope.launch {
                    val now = nowLocalTime(clock)
                    val endTime = clampEndTimeNotBeforeStart(draft.startTime, now)

                    val startMillis = localTimeToEpochMillis(today, draft.startTime)
                    val endMillis = localTimeToEpochMillis(today, endTime)

                    savedExerciseActivityUseCase(
                        type = draft.activityType,
                        startTimestamp = startMillis,
                        endTimestamp = endMillis,
                        notes = draft.notes.ifBlank { null },
                        intensity = draft.intensity
                    )

                    // close sheet
                    _state.update { it.copy(exerciseDraft = null) }
                }
            }

            TodayScreenContract.Intent.DismissExerciseSheet -> {
                _state.update { it.copy(exerciseDraft = null) }
            }

            is TodayScreenContract.Intent.SupplementLogOptionSelected -> {
                viewModelScope.launch {
                    val scheduledOrNull =
                        when (intent.option) {
                            TodayScreenContract.SupplementLogOption.Scheduled -> intent.scheduledTime
                            TodayScreenContract.SupplementLogOption.NowExtra -> null
                        }

                    _effect.send(
                        ShowDoseInputDialog(
                            supplementId = intent.supplementId,
                            title = intent.title,
                            defaultUnit = intent.defaultUnit,
                            suggestedDose = intent.suggestedDose,
                            scheduledTime = scheduledOrNull
                        )
                    )
                }
            }

            is TodayScreenContract.Intent.LogMealConfirmed -> {
                viewModelScope.launch {
                    logMealUseCase(
                        input = LogMealInput(
                            mealType = intent.input.mealType,
                            notes = intent.input.notes,
                            nutrition = intent.input.nutrition?.toDomain(),
                            timeUseIntent = TimeUseIntent.ActualNow,
                        ),
                        clock = Clock.System
                    )
                }
            }

            is TodayScreenContract.Intent.LogMealTapped -> TODO()
        }
    }

    private fun epochMillisToLocalDateTime(
        utcMillis: Long
    ): LocalDateTime =
        Instant
            .fromEpochMilliseconds(utcMillis)
            .toLocalDateTime(DomainTimePolicy.localTimeZone)

    fun TodayScreenContract.NutritionInput.toDomain(): NutritionInput =
        NutritionInput(
            calories = calories?.toInt(),
            proteinGrams = proteinGrams,
            carbsGrams = carbsGrams,
            fatGrams = fatGrams,
            sodiumMg = sodiumMg,
            cholesterolMg = cholesterolMg,
            fiberGrams = fiberGrams
        )

    private fun handleTimelineItemClicked(
        uiItem: TimelineItemUiModel
    ) {
        val domainItem = findDomainItemFor(uiItem) ?: return

        when (val action = handleTimelineItemTapUseCase.resolve(uiItem)) {

            is TimelineTapAction.RequestDoseInput -> {
                viewModelScope.launch {
                    _effect.send(
                        ShowSupplementLogChoice(
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
        val ui = item as? ActivityUiModel ?: return
        val now = DomainTimePolicy.nowLocalDateTime(clock).time

        val draft = ExerciseDraft(
            activityType = ui.activityType,
            startTime = now,
            endTime = ui.endTime,
            notes = "",
            intensity = null,
            phase = ExerciseDraft.Phase.Draft
        )
        setExerciseDraft(draft)
    }

    /**
     * Writes the exercise draft into:
     * 1) State (to drive the Compose UI), and
     * 2) SavedStateHandle (to survive process recreation).
     */
    private fun setExerciseDraft(draft: ExerciseDraft) {
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

        val draft = ExerciseDraft(
            activityType = ActivityType.valueOf(type),
            startTime = minutesToLocalTime(startMin),
            endTime = endMin?.let(::minutesToLocalTime),
            notes = notes,
            intensity = intensity,
            phase = ExerciseDraft.Phase.valueOf(phaseRaw)
        )

        _state.update { it.copy(exerciseDraft = draft) }
    }

    /** Persists the draft into small primitives so Android can restore it after process recreation. */
    private fun persistExerciseDraft(draft: ExerciseDraft) {
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

    private fun nowLocalTime(clock: Clock): LocalTime =
        DomainTimePolicy.nowLocalDateTime(clock).time

    private fun localTimeToEpochMillis(
        date: LocalDate,
        time: LocalTime
    ): Long {
        val ldt = LocalDateTime(date = date, time = time)
        return ldt.toInstant(DomainTimePolicy.localTimeZone).toEpochMilliseconds()
    }

    /**
     * Ensures endTime is never earlier than startTime.
     * If "now" is earlier (rare but possible with manual edits / clock changes),
     * clamp to startTime.
     */
    private fun clampEndTimeNotBeforeStart(
        start: LocalTime,
        endCandidate: LocalTime
    ): LocalTime = if (endCandidate < start) start else endCandidate

}
