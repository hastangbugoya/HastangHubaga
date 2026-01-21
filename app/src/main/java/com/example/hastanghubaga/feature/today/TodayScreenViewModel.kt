package com.example.hastanghubaga.feature.today

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hastanghubaga.domain.model.activity.ActivityType
import com.example.hastanghubaga.domain.model.meal.LogMealInput
import com.example.hastanghubaga.domain.model.meal.NutritionInput
import com.example.hastanghubaga.domain.model.timeline.LogDoseInput
import com.example.hastanghubaga.domain.time.DomainTimePolicy
import com.example.hastanghubaga.domain.time.TimeUseIntent
import com.example.hastanghubaga.domain.usecase.activity.GetActivitiesForDateUseCase
import com.example.hastanghubaga.domain.usecase.activity.SaveExerciseActivityUseCase
import com.example.hastanghubaga.domain.usecase.meal.GetMealsForDateUseCase
import com.example.hastanghubaga.domain.usecase.meal.LogMealUseCase
import com.example.hastanghubaga.domain.usecase.supplement.GetSupplementsWithUserSettingsForDateUseCase
import com.example.hastanghubaga.domain.usecase.todaytimeline.BuildTodayTimelineUseCase
import com.example.hastanghubaga.domain.usecase.todaytimeline.HandleTimelineItemTapUseCase
import com.example.hastanghubaga.domain.usecase.todaytimeline.LogSupplementDoseUseCase
import com.example.hastanghubaga.domain.usecase.todaytimeline.TimelineTapAction
import com.example.hastanghubaga.feature.today.TodayScreenContract.Effect
import com.example.hastanghubaga.feature.today.TodayScreenContract.Effect.ShowDoseInputDialog
import com.example.hastanghubaga.feature.today.TodayScreenContract.Effect.ShowSupplementLogChoice
import com.example.hastanghubaga.feature.today.TodayScreenContract.ExerciseDraft
import com.example.hastanghubaga.ui.timeline.ActivityUiModel
import com.example.hastanghubaga.ui.timeline.SupplementDoseLogUiModel
import com.example.hastanghubaga.ui.timeline.TimelineItem
import com.example.hastanghubaga.ui.timeline.TimelineItemUiModel
import com.example.hastanghubaga.ui.timeline.TodayUiRowType
import com.example.hastanghubaga.ui.timeline.toTimelineItemUiModels
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
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

/**
 * TodayScreenViewModel drives the Today timeline UI (supplements, meals, activities, dose logs)
 * and the sheet-based interaction flows (supplement logging, exercise start/confirm, meal logging).
 *
 * ---
 * ## Problem solved (2026-01)
 * The timeline would intermittently render as empty (e.g. "Rendering 0 items") and feel "broken",
 * especially after switching panels/screens.
 *
 * Root cause:
 * - A previous implementation cancelled a long-lived Flow collection job and then relied on
 *   recomposition/navigation timing to restart it. In some cases the upstream flows did not
 *   re-emit, leaving state stuck empty.
 *
 * Fix:
 * - The timeline is collected exactly once in [init] via a single pipeline driven by a
 *   [selectedDate] StateFlow.
 * - Changing dates (or "refresh") updates [selectedDate] only; it does not create new collectors.
 * - Heavy timeline building runs off the main thread via [flowOn] to avoid jank / skipped frames.
 *
 * ---
 * ## Tips to avoid reintroducing this bug
 * - Do NOT cancel and restart terminal Flow collections unless you are done forever.
 * - Avoid starting new `viewModelScope.launch { flow.collect { ... } }` inside user intents.
 *   Prefer a single long-lived collector (or explicit `shareIn/stateIn`) and drive it with state.
 * - If you must catch exceptions around flows, rethrow kotlinx.coroutines.CancellationException so
 *   cancellation remains cooperative.
 * - Keep heavy sorting/mapping/building off main (`Dispatchers.Default`), and IO on `Dispatchers.IO`.
 */
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

    private val _effect = Channel<Effect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    /**
     * Single source of truth for which day the Today timeline is displaying.
     * Changing this updates the long-lived timeline collector via `flatMapLatest`.
     */
    private val selectedDate = MutableStateFlow(DomainTimePolicy.todayLocal())

    /**
     * SavedStateHandle keys for persisting the “in-progress exercise draft”.
     * Times are stored as "minutes since midnight" for stability.
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
        observeTimeline()
        Log.d("Meow", "TodayVM init: ${hashCode()}")
    }

    override fun onCleared() {
        Log.d("Meow", "TodayVM cleared: ${hashCode()}")
        super.onCleared()
    }

    fun onIntent(intent: TodayScreenContract.Intent) {
        val clock = Clock.System
        val today = DomainTimePolicy.todayLocal(clock)

        when (intent) {
            is TodayScreenContract.Intent.LoadToday -> loadToday(today)
            is TodayScreenContract.Intent.Refresh -> loadToday(today)

            is TodayScreenContract.Intent.TimelineItemClicked -> {
                handleTimelineItemClicked(intent.item)
            }

            is TodayScreenContract.Intent.ConfirmDose -> {
                viewModelScope.launch {
                    val timeUseIntent =
                        when {
                            intent.actualTime != null ->
                                TimeUseIntent.Explicit(today, intent.actualTime)
                            intent.scheduledTime != null ->
                                TimeUseIntent.Scheduled(intent.scheduledTime)
                            else ->
                                TimeUseIntent.ActualNow
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

                setExerciseDraft(
                    ExerciseDraft(
                        activityType = activityUi.activityType,
                        startTime = start,
                        endTime = null,
                        notes = "",
                        intensity = null,
                        phase = ExerciseDraft.Phase.Draft
                    )
                )
            }

            TodayScreenContract.Intent.ExerciseStartPressed -> {
                val existing = state.value.exerciseDraft ?: return
                if (existing.phase == ExerciseDraft.Phase.Running) return

                setExerciseDraft(
                    existing.copy(
                        endTime = null,
                        phase = ExerciseDraft.Phase.Running
                    )
                )
            }

            is TodayScreenContract.Intent.ExerciseNotesChanged -> {
                val existing = state.value.exerciseDraft ?: return
                setExerciseDraft(existing.copy(notes = intent.value))
            }

            is TodayScreenContract.Intent.ExerciseIntensityChanged -> {
                val existing = state.value.exerciseDraft ?: return
                setExerciseDraft(existing.copy(intensity = intent.value))
            }

            is TodayScreenContract.Intent.ExerciseEndTimeChanged -> {
                val existing = state.value.exerciseDraft ?: return
                val clamped =
                    intent.value?.let { clampEndTimeNotBeforeStart(existing.startTime, it) }
                setExerciseDraft(existing.copy(endTime = clamped))
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

                    clearExerciseDraft()
                }
            }

            TodayScreenContract.Intent.DismissExerciseSheet -> clearExerciseDraft()

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

    /**
     * Public entry point for changing which day is displayed.
     * This does not create new collectors; it only updates [selectedDate].
     */
    fun loadToday(date: LocalDate = DomainTimePolicy.todayLocal()) {
        Log.d("Meow", "TodayVM loadToday(day=$date)")
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        selectedDate.value = date
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeTimeline() {
        viewModelScope.launch {
            selectedDate
                .flatMapLatest { date ->
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
                    }
                        .flowOn(Dispatchers.Default)
                }
                .collect { timeline ->
                    Log.d("Meow", "Timeline update size=${timeline.size}")
                    _state.update {
                        it.copy(
                            isLoading = false,
                            domainTimelineItems = timeline,
                            uiTimelineItems = timeline.toTimelineItemUiModels()
                        )
                    }
                }
        }
    }

    private fun epochMillisToLocalDateTime(utcMillis: Long): LocalDateTime =
        Instant
            .fromEpochMilliseconds(utcMillis)
            .toLocalDateTime(DomainTimePolicy.localTimeZone)

    private fun TodayScreenContract.NutritionInput.toDomain(): NutritionInput =
        NutritionInput(
            calories = calories?.toInt(),
            proteinGrams = proteinGrams,
            carbsGrams = carbsGrams,
            fatGrams = fatGrams,
            sodiumMg = sodiumMg,
            cholesterolMg = cholesterolMg,
            fiberGrams = fiberGrams
        )

    private fun handleTimelineItemClicked(uiItem: TimelineItemUiModel) {
        // Domain lookup retained for continuity (even if not used yet).
        findDomainItemFor(uiItem) ?: return

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

            is TimelineTapAction.ActivityTapped -> Unit
            is TimelineTapAction.MealTapped -> Unit
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

    /**
     * Resolves a UI row back to its corresponding domain timeline item.
     *
     * For scheduled items (supplement/activity/meal), (id + time) is enough.
     *
     * For dose log rows, the domain item currently has no `doseLogId`, so we match using:
     * - supplementId
     * - time
     * - scheduledTime (nullable)
     * - amount/unit when parseable
     *
     * If you later add a real doseLogId to the domain item, update this method to match on it.
     */
    private fun findDomainItemFor(uiItem: TimelineItemUiModel): TimelineItem? {
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

                identity.type == TodayUiRowType.SUPPLEMENT_DOSE_LOG &&
                        uiItem is SupplementDoseLogUiModel &&
                        domainItem is TimelineItem.SupplementDoseLogTimelineItem -> {
                    val uiAmount = uiItem.amountText?.toDoubleOrNull()
                    val domainAmount = domainItem.amount

                    val amountMatches =
                        when {
                            uiAmount == null || domainAmount == null -> true
                            else -> kotlin.math.abs(uiAmount - domainAmount) < 0.0001
                        }

                    val unitMatches =
                        when {
                            uiItem.unitText.isNullOrBlank() || domainItem.unit.isNullOrBlank() -> true
                            else -> uiItem.unitText.equals(domainItem.unit, ignoreCase = true)
                        }

                    domainItem.supplementId == uiItem.supplementId &&
                            domainItem.time == uiItem.time &&
                            domainItem.scheduledTime == uiItem.scheduledTime &&
                            amountMatches &&
                            unitMatches
                }

                else -> false
            }
        }
    }

    // ---- Exercise draft helpers (State + SavedStateHandle) ----

    private fun setExerciseDraft(draft: ExerciseDraft) {
        _state.update { it.copy(exerciseDraft = draft) }
        persistExerciseDraft(draft)
    }

    private fun clearExerciseDraft() {
        _state.update { it.copy(exerciseDraft = null) }
        clearPersistedExerciseDraft()
    }

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

    private fun persistExerciseDraft(draft: ExerciseDraft) {
        savedStateHandle[ExerciseSavedStateKeys.TYPE] = draft.activityType.name
        savedStateHandle[ExerciseSavedStateKeys.START_MIN] = draft.startTime.toMinutesOfDay()
        savedStateHandle[ExerciseSavedStateKeys.END_MIN] = draft.endTime?.toMinutesOfDay()
        savedStateHandle[ExerciseSavedStateKeys.NOTES] = draft.notes
        savedStateHandle[ExerciseSavedStateKeys.INTENSITY] = draft.intensity
        savedStateHandle[ExerciseSavedStateKeys.PHASE] = draft.phase.name
    }

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

    private fun localTimeToEpochMillis(date: LocalDate, time: LocalTime): Long {
        val ldt = LocalDateTime(date = date, time = time)
        return ldt.toInstant(DomainTimePolicy.localTimeZone).toEpochMilliseconds()
    }

    private fun clampEndTimeNotBeforeStart(start: LocalTime, endCandidate: LocalTime): LocalTime =
        if (endCandidate < start) start else endCandidate
}
