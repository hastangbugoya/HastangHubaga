package com.example.hastanghubaga.feature.today

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hastanghubaga.data.local.entity.activity.ActivityOccurrenceEntity
import com.example.hastanghubaga.data.local.entity.meal.AkImportedMealEntity
import com.example.hastanghubaga.data.local.entity.supplement.SupplementOccurrenceEntity
import com.example.hastanghubaga.domain.model.activity.Activity
import com.example.hastanghubaga.domain.model.activity.ActivityType
import com.example.hastanghubaga.domain.model.meal.LogMealInput
import com.example.hastanghubaga.domain.model.meal.Meal
import com.example.hastanghubaga.domain.model.meal.NutritionInput
import com.example.hastanghubaga.domain.model.supplement.Supplement
import com.example.hastanghubaga.domain.model.supplement.SupplementDoseLog
import com.example.hastanghubaga.domain.model.timeline.LogDoseInput
import com.example.hastanghubaga.domain.time.DomainTimePolicy
import com.example.hastanghubaga.domain.time.TimeUseIntent
import com.example.hastanghubaga.domain.usecase.activity.GetActivitiesForDateUseCase
import com.example.hastanghubaga.domain.usecase.activity.GetActivityOccurrencesForDateUseCase
import com.example.hastanghubaga.domain.usecase.activity.MaterializeActivityOccurrencesForDateUseCase
import com.example.hastanghubaga.domain.usecase.activity.SaveExerciseActivityUseCase
import com.example.hastanghubaga.domain.usecase.meal.GetImportedMealsForDateUseCase
import com.example.hastanghubaga.domain.usecase.meal.GetMealsForDateUseCase
import com.example.hastanghubaga.domain.usecase.meal.LogMealUseCase
import com.example.hastanghubaga.domain.usecase.supplement.GetActiveSupplementsUseCase
import com.example.hastanghubaga.domain.usecase.supplement.GetSupplementDoseLogsForDateUseCase
import com.example.hastanghubaga.domain.usecase.supplement.GetSupplementOccurrencesForDateUseCase
import com.example.hastanghubaga.domain.usecase.supplement.MaterializeSupplementOccurrencesForDateUseCase
import com.example.hastanghubaga.domain.usecase.todaytimeline.BuildTodayTimelineUseCase
import com.example.hastanghubaga.domain.usecase.todaytimeline.HandleTimelineItemTapUseCase
import com.example.hastanghubaga.domain.usecase.todaytimeline.LogSupplementDoseUseCase
import com.example.hastanghubaga.domain.usecase.todaytimeline.TimelineTapAction
import com.example.hastanghubaga.feature.today.TodayScreenContract.Effect
import com.example.hastanghubaga.feature.today.TodayScreenContract.Effect.ShowDoseInputDialog
import com.example.hastanghubaga.feature.today.TodayScreenContract.Effect.ShowForceLogSupplementPicker
import com.example.hastanghubaga.feature.today.TodayScreenContract.Effect.ShowSupplementLogChoice
import com.example.hastanghubaga.feature.today.TodayScreenContract.ExerciseDraft
import com.example.hastanghubaga.ui.timeline.ActivityUiModel
import com.example.hastanghubaga.ui.timeline.SupplementDoseLogUiModel
import com.example.hastanghubaga.ui.timeline.SupplementUiModel
import com.example.hastanghubaga.ui.timeline.TimelineItem
import com.example.hastanghubaga.ui.timeline.TimelineItemUiModel
import com.example.hastanghubaga.ui.timeline.TodayUiRowType
import com.example.hastanghubaga.ui.timeline.toTimelineItemUiModels
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
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

@HiltViewModel
class TodayScreenViewModel @Inject constructor(
    private val getSupplementOccurrencesForDate: GetSupplementOccurrencesForDateUseCase,
    private val getActiveSupplements: GetActiveSupplementsUseCase,
    private val getSupplementDoseLogsForDate: GetSupplementDoseLogsForDateUseCase,
    private val getMealsForDate: GetMealsForDateUseCase,
    private val getImportedMealsForDate: GetImportedMealsForDateUseCase,
    private val getActivityOccurrencesForDate: GetActivityOccurrencesForDateUseCase,
    private val getActivitiesForDate: GetActivitiesForDateUseCase,
    private val materializeSupplementOccurrencesForDate: MaterializeSupplementOccurrencesForDateUseCase,
    private val materializeActivityOccurrencesForDate: MaterializeActivityOccurrencesForDateUseCase,
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

    private val selectedDate = MutableStateFlow(DomainTimePolicy.todayLocal())

    private object ExerciseSavedStateKeys {
        const val TYPE = "exercise.type"
        const val START_MIN = "exercise.startMin"
        const val END_MIN = "exercise.endMin"
        const val NOTES = "exercise.notes"
        const val INTENSITY = "exercise.intensity"
        const val PHASE = "exercise.phase"
    }

    init {
        restoreExerciseDraftIfPresent()
        _state.update { it.copy(selectedDate = selectedDate.value) }
        materializeSelectedDate(selectedDate.value)
        observeTimeline()
        Log.d("Meow", "TodayVM init: ${hashCode()}")
    }

    override fun onCleared() {
        Log.d("Meow", "TodayVM cleared: ${hashCode()}")
        super.onCleared()
    }

    fun onIntent(intent: TodayScreenContract.Intent) {
        val clock = Clock.System

        when (intent) {
            is TodayScreenContract.Intent.LoadDate -> loadToday(intent.date)
            is TodayScreenContract.Intent.Refresh -> {
                materializeSelectedDate(selectedDate.value)
                loadToday(selectedDate.value)
            }

            is TodayScreenContract.Intent.TimelineItemClicked -> {
                handleTimelineItemClicked(intent.item)
            }

            TodayScreenContract.Intent.ForceLogSupplementTapped -> {
                viewModelScope.launch {
                    val supplements = getActiveSupplements().first()
                    _effect.send(
                        ShowForceLogSupplementPicker(
                            supplements = supplements
                        )
                    )
                }
            }

            is TodayScreenContract.Intent.ForceLogSupplementSelected -> {
                val initialDate = selectedDate.value
                val initialTime = nowLocalTime(clock)

                viewModelScope.launch {
                    _effect.send(
                        ShowDoseInputDialog(
                            supplementId = intent.supplementId,
                            title = intent.title,
                            scheduledTime = null,
                            defaultUnit = intent.defaultUnit,
                            suggestedDose = intent.suggestedDose,
                            occurrenceId = null,
                            initialActualDate = initialDate,
                            initialActualTime = initialTime
                        )
                    )
                }
            }

            is TodayScreenContract.Intent.ConfirmDose -> {
                viewModelScope.launch {
                    val explicitDate = intent.actualDate ?: selectedDate.value

                    val timeUseIntent =
                        when {
                            intent.actualTime != null ->
                                TimeUseIntent.Explicit(explicitDate, intent.actualTime)
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
                            timeUseIntent = timeUseIntent,
                            occurrenceId = intent.occurrenceId,
                            plannedTime = intent.scheduledTime
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

                    val activeDate = selectedDate.value
                    val startMillis = localTimeToEpochMillis(activeDate, draft.startTime)
                    val endMillis = localTimeToEpochMillis(activeDate, endTime)

                    savedExerciseActivityUseCase(
                        type = draft.activityType,
                        startTimestamp = startMillis,
                        endTimestamp = endMillis,
                        notes = draft.notes.ifBlank { null },
                        intensity = draft.intensity
                    )

                    materializeSelectedDate(selectedDate.value)
                    clearExerciseDraft()
                }
            }

            TodayScreenContract.Intent.DismissExerciseSheet -> clearExerciseDraft()

            is TodayScreenContract.Intent.SupplementLogOptionSelected -> {
                val initialDate = selectedDate.value
                val initialTime = intent.scheduledTime ?: nowLocalTime(clock)

                viewModelScope.launch {
                    val scheduledOrNull =
                        when (intent.option) {
                            TodayScreenContract.SupplementLogOption.Scheduled -> intent.scheduledTime
                            TodayScreenContract.SupplementLogOption.NowExtra -> null
                        }

                    val occurrenceIdOrNull =
                        when (intent.option) {
                            TodayScreenContract.SupplementLogOption.Scheduled -> intent.occurrenceId
                            TodayScreenContract.SupplementLogOption.NowExtra -> null
                        }

                    _effect.send(
                        ShowDoseInputDialog(
                            supplementId = intent.supplementId,
                            title = intent.title,
                            defaultUnit = intent.defaultUnit,
                            suggestedDose = intent.suggestedDose,
                            scheduledTime = scheduledOrNull,
                            occurrenceId = occurrenceIdOrNull,
                            initialActualDate = initialDate,
                            initialActualTime = initialTime
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
                    materializeSelectedDate(selectedDate.value)
                }
            }

            is TodayScreenContract.Intent.LogMealTapped -> TODO()
        }
    }

    fun loadToday(date: LocalDate = DomainTimePolicy.todayLocal()) {
        if (selectedDate.value == date) {
            Log.d("Meow", "TodayVM loadToday ignored (same date=$date)")
            _state.update {
                it.copy(
                    isLoading = false,
                    errorMessage = null,
                    selectedDate = date
                )
            }
            return
        }

        Log.d("Meow", "TodayVM loadToday(day=$date)")
        _state.update {
            it.copy(
                isLoading = true,
                errorMessage = null,
                selectedDate = date
            )
        }
        materializeSelectedDate(date)
        selectedDate.value = date
    }

    private data class TimelineInputs(
        val supplementOccurrences: List<SupplementOccurrenceEntity>,
        val supplements: List<Supplement>,
        val supplementDoseLogs: List<SupplementDoseLog>,
        val meals: List<Meal>,
        val importedMeals: List<AkImportedMealEntity>,
        val activityOccurrences: List<ActivityOccurrenceEntity>,
        val activities: List<Activity>
    )

    private fun observeTimeline() {
        Log.d("Meow", "TodayVM observeTimeline()")
        viewModelScope.launch {
            selectedDate
                .flatMapLatest { date ->
                    combine(
                        combine(
                            getSupplementOccurrencesForDate(date),
                            getActiveSupplements(),
                            getSupplementDoseLogsForDate(date),
                            getMealsForDate(date)
                        ) { supplementOccurrences, supplements, supplementDoseLogs, meals ->
                            QuadA(
                                supplementOccurrences = supplementOccurrences,
                                supplements = supplements,
                                supplementDoseLogs = supplementDoseLogs,
                                meals = meals
                            )
                        },
                        combine(
                            getImportedMealsForDate(date),
                            getActivityOccurrencesForDate(date),
                            getActivitiesForDate(date)
                        ) { importedMeals, activityOccurrences, activities ->
                            TripleB(
                                importedMeals = importedMeals,
                                activityOccurrences = activityOccurrences,
                                activities = activities
                            )
                        }
                    ) { a, b ->
                        TimelineInputs(
                            supplementOccurrences = a.supplementOccurrences,
                            supplements = a.supplements,
                            supplementDoseLogs = a.supplementDoseLogs,
                            meals = a.meals,
                            importedMeals = b.importedMeals,
                            activityOccurrences = b.activityOccurrences,
                            activities = b.activities
                        )
                    }.map { inputs ->
                        buildTodayTimeline(
                            date = date,
                            supplementOccurrences = inputs.supplementOccurrences,
                            supplements = inputs.supplements,
                            supplementDoseLogs = inputs.supplementDoseLogs,
                            meals = inputs.meals,
                            importedMeals = inputs.importedMeals,
                            activityOccurrences = inputs.activityOccurrences,
                            activities = inputs.activities
                        )
                    }.flowOn(Dispatchers.Default)
                }
                .collect { timeline ->
                    Log.d("Meow", "Timeline update size=${timeline.size}")
                    _state.update {
                        it.copy(
                            selectedDate = selectedDate.value,
                            isLoading = false,
                            domainTimelineItems = timeline,
                            uiTimelineItems = timeline.toTimelineItemUiModels()
                        )
                    }
                }
        }
    }

    private data class QuadA(
        val supplementOccurrences: List<SupplementOccurrenceEntity>,
        val supplements: List<Supplement>,
        val supplementDoseLogs: List<SupplementDoseLog>,
        val meals: List<Meal>
    )

    private data class TripleB(
        val importedMeals: List<AkImportedMealEntity>,
        val activityOccurrences: List<ActivityOccurrenceEntity>,
        val activities: List<Activity>
    )

    private fun materializeSelectedDate(date: LocalDate) {
        viewModelScope.launch(Dispatchers.IO) {
            val meals = getMealsForDate(date).first()
            val importedMeals = getImportedMealsForDate(date).first()

            materializeSupplementOccurrencesForDate(
                date = date,
                meals = meals,
                importedMeals = importedMeals
            )

            materializeActivityOccurrencesForDate(
                date = date
            )
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
                            scheduledTime = action.scheduledTime,
                            occurrenceId = action.occurrenceId
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

    private fun findDomainItemFor(uiItem: TimelineItemUiModel): TimelineItem? {
        val identity = uiItem.identity()

        return state.value.domainTimelineItems.firstOrNull { domainItem ->
            when {
                identity.type == TodayUiRowType.SUPPLEMENT &&
                        uiItem is SupplementUiModel &&
                        domainItem is TimelineItem.SupplementTimelineItem ->
                    domainItem.supplementId == identity.id &&
                            domainItem.time == identity.time &&
                            domainItem.occurrenceId == (uiItem.occurrenceId ?: domainItem.occurrenceId)

                identity.type == TodayUiRowType.ACTIVITY &&
                        domainItem is TimelineItem.ActivityTimelineItem ->
                    domainItem.activityId == identity.id &&
                            domainItem.time == identity.time

                identity.type == TodayUiRowType.MEAL &&
                        domainItem is TimelineItem.MealTimelineItem ->
                    domainItem.meal.id == identity.id &&
                            domainItem.time == identity.time

                identity.type == TodayUiRowType.MEAL &&
                        domainItem is TimelineItem.ImportedMealTimelineItem ->
                    importedMealStableId(domainItem.meal.groupingKey) == identity.id &&
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

    private fun importedMealStableId(groupingKey: String): Long {
        val positive = groupingKey.hashCode().toLong() and 0x7fffffffL
        return -positive.coerceAtLeast(1L)
    }

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