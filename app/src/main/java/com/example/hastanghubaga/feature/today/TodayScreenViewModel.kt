package com.example.hastanghubaga.feature.today

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hastanghubaga.data.local.entity.activity.ActivityOccurrenceEntity
import com.example.hastanghubaga.data.local.entity.meal.AkImportedMealEntity
import com.example.hastanghubaga.data.local.entity.meal.MealOccurrenceEntity
import com.example.hastanghubaga.data.local.entity.meal.MealType
import com.example.hastanghubaga.data.local.entity.supplement.SupplementOccurrenceEntity
import com.example.hastanghubaga.domain.model.activity.Activity
import com.example.hastanghubaga.domain.model.activity.ActivityLog
import com.example.hastanghubaga.domain.model.activity.ActivityType
import com.example.hastanghubaga.domain.model.meal.Meal
import com.example.hastanghubaga.domain.model.meal.MealLog
import com.example.hastanghubaga.domain.model.meal.NutritionInput
import com.example.hastanghubaga.domain.model.supplement.Supplement
import com.example.hastanghubaga.domain.model.supplement.SupplementDoseLog
import com.example.hastanghubaga.domain.model.timeline.LogDoseInput
import com.example.hastanghubaga.domain.repository.activity.ActivityLogRepository
import com.example.hastanghubaga.domain.repository.meal.MealLogRepository
import com.example.hastanghubaga.domain.time.DomainTimePolicy
import com.example.hastanghubaga.domain.time.TimeUseIntent
import com.example.hastanghubaga.domain.usecase.activity.GetActivitiesForDateUseCase
import com.example.hastanghubaga.domain.usecase.activity.GetActivityOccurrencesForDateUseCase
import com.example.hastanghubaga.domain.usecase.activity.MaterializeActivityOccurrencesForDateUseCase
import com.example.hastanghubaga.domain.usecase.activity.SaveExerciseActivityUseCase
import com.example.hastanghubaga.domain.usecase.meal.GetImportedMealsForDateUseCase
import com.example.hastanghubaga.domain.usecase.meal.GetMealOccurrencesForDateUseCase
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
import com.example.hastanghubaga.feature.today.TodayScreenContract.MealLogInput
import com.example.hastanghubaga.ui.timeline.ActivityUiModel
import com.example.hastanghubaga.ui.timeline.MealUiModel
import com.example.hastanghubaga.ui.timeline.SupplementDoseLogUiModel
import com.example.hastanghubaga.ui.timeline.SupplementUiModel
import com.example.hastanghubaga.ui.timeline.TimelineItem
import com.example.hastanghubaga.ui.timeline.TimelineItemUiModel
import com.example.hastanghubaga.ui.timeline.TodayUiRowType
import com.example.hastanghubaga.ui.timeline.toTimelineItemUiModels
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
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
    private val getMealOccurrencesForDate: GetMealOccurrencesForDateUseCase,
    private val getImportedMealsForDate: GetImportedMealsForDateUseCase,
    private val getActivityOccurrencesForDate: GetActivityOccurrencesForDateUseCase,
    private val getActivitiesForDate: GetActivitiesForDateUseCase,
    private val activityLogRepository: ActivityLogRepository,
    private val mealLogRepository: MealLogRepository,
    private val materializeSupplementOccurrencesForDate: MaterializeSupplementOccurrencesForDateUseCase,
    private val materializeActivityOccurrencesForDate: MaterializeActivityOccurrencesForDateUseCase,
    private val buildTodayTimeline: BuildTodayTimelineUseCase,
    private val handleTimelineItemTapUseCase: HandleTimelineItemTapUseCase,
    private val logSupplementDoseUseCase: LogSupplementDoseUseCase,
    private val savedStateHandle: SavedStateHandle,
    private val saveExerciseActivityUseCase: SaveExerciseActivityUseCase,
    private val logMealUseCase: LogMealUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(TodayScreenContract.State())
    val state: StateFlow<TodayScreenContract.State> = _state

    private val _effect = Channel<Effect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private val selectedDate = MutableStateFlow(DomainTimePolicy.todayLocal())

    private object ExerciseSavedStateKeys {
        const val ACTIVITY_ID = "exercise.activityId"
        const val TYPE = "exercise.type"
        const val LOG_DATE = "exercise.logDate"
        const val START_MIN = "exercise.startMin"
        const val END_MIN = "exercise.endMin"
        const val NOTES = "exercise.notes"
        const val INTENSITY = "exercise.intensity"
        const val OCCURRENCE_ID = "exercise.occurrenceId"
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
                val logDate = selectedDate.value
                val start = activityUi.time
                val end = addDefaultDuration(start)

                Log.d(
                    "ACTIVITY_RECON",
                    "exercise tapped ui id=${activityUi.id} activityId=${activityUi.activityId} occurrenceId=${activityUi.occurrenceId} title=${activityUi.title} time=${activityUi.time}"
                )
                Log.d(
                    "ACTIVITY_RECON",
                    "draft create activityId=${activityUi.id} occurrenceId=${activityUi.occurrenceId}"
                )

                setExerciseDraft(
                    ExerciseDraft(
                        activityId = activityUi.id,
                        activityType = activityUi.activityType,
                        logDate = logDate,
                        startTime = start,
                        endTime = end,
                        notes = "",
                        intensity = 0,
                        occurrenceId = activityUi.occurrenceId
                    )
                )
            }

            is TodayScreenContract.Intent.ExerciseDateChanged -> {
                val existing = state.value.exerciseDraft ?: return
                Log.d(
                    "ACTIVITY_RECON",
                    "date change existing occurrenceId=${existing.occurrenceId} newDate=${intent.value}"
                )
                setExerciseDraft(existing.copy(logDate = intent.value))
            }

            is TodayScreenContract.Intent.ExerciseStartTimeChanged -> {
                val existing = state.value.exerciseDraft ?: return
                val adjustedEnd = clampEndTimeNotBeforeStart(intent.value, existing.endTime)
                Log.d(
                    "ACTIVITY_RECON",
                    "start change existing occurrenceId=${existing.occurrenceId} newStart=${intent.value} adjustedEnd=$adjustedEnd"
                )
                setExerciseDraft(
                    existing.copy(
                        startTime = intent.value,
                        endTime = adjustedEnd
                    )
                )
            }

            is TodayScreenContract.Intent.ExerciseEndTimeChanged -> {
                val existing = state.value.exerciseDraft ?: return
                val clamped = clampEndTimeNotBeforeStart(existing.startTime, intent.value)
                Log.d(
                    "ACTIVITY_RECON",
                    "end change existing occurrenceId=${existing.occurrenceId} newEnd=${intent.value} clampedEnd=$clamped"
                )
                setExerciseDraft(existing.copy(endTime = clamped))
            }

            is TodayScreenContract.Intent.ExerciseNotesChanged -> {
                val existing = state.value.exerciseDraft ?: return
                Log.d(
                    "ACTIVITY_RECON",
                    "notes change existing occurrenceId=${existing.occurrenceId} notesLength=${intent.value.length}"
                )
                setExerciseDraft(existing.copy(notes = intent.value))
            }

            is TodayScreenContract.Intent.ExerciseIntensityChanged -> {
                val existing = state.value.exerciseDraft ?: return
                Log.d(
                    "ACTIVITY_RECON",
                    "intensity change existing occurrenceId=${existing.occurrenceId} intensity=${intent.value}"
                )
                setExerciseDraft(existing.copy(intensity = intent.value ?: 0))
            }

            TodayScreenContract.Intent.ExerciseConfirmPressed -> {
                val draft = state.value.exerciseDraft ?: return

                viewModelScope.launch {
                    val startMillis = localTimeToEpochMillis(draft.logDate, draft.startTime)
                    val endMillis = localTimeToEpochMillis(draft.logDate, draft.endTime)
                    Log.d(
                        "ACTIVITY_RECON",
                        "saving log activityId=${draft.activityId} occurrenceId=${draft.occurrenceId} type=${draft.activityType} intensity=${draft.intensity}"
                    )
                    saveExerciseActivityUseCase(
                        activityId = draft.activityId,
                        occurrenceId = draft.occurrenceId,
                        type = draft.activityType,
                        startTimestamp = startMillis,
                        endTimestamp = endMillis,
                        notes = draft.notes.ifBlank { null },
                        intensity = draft.intensity ?: 0
                    )

                    materializeSelectedDate(selectedDate.value)
                    if (draft.logDate != selectedDate.value) {
                        materializeSelectedDate(draft.logDate)
                    }

                    clearExerciseDraft()
                }
            }

            TodayScreenContract.Intent.DismissExerciseSheet -> {
                clearExerciseDraft()
            }

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
                val draft = state.value.mealDraft ?: intent.input
                Log.d("MEAL_RECON", "TodayScreenViewModel.LogMealConfirmed> input: $draft")
                viewModelScope.launch {
                    logMealUseCase(
                        input = TodayScreenContract.run { draft.toDomain() },
                        clock = Clock.System
                    )

                    materializeSelectedDate(selectedDate.value)
                    if (draft.logDate != selectedDate.value) {
                        materializeSelectedDate(draft.logDate)
                    }

                    clearMealDraft()
                }
            }

            is TodayScreenContract.Intent.LogMealTapped -> {
                val item: MealUiModel = intent.item
                val logDate = selectedDate.value
                val start = item.time
                val end = addDefaultDuration(start)

                Log.d(
                    "MEAL_RECON",
                    "tap using UI item occurrenceId=${item.occurrenceId} mealType=${item.mealType} time=${item.time}"
                )

                setMealDraft(
                    MealLogInput(
                        mealType = item.mealType,
                        logDate = logDate,
                        startTime = start,
                        endTime = end,
                        notes = null,
                        nutrition = null,
                        occurrenceId = item.occurrenceId
                    )
                )
            }

            TodayScreenContract.Intent.DismissMealSheet -> {
                clearMealDraft()
            }
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
        val mealOccurrences: List<MealOccurrenceEntity>,
        val meals: List<Meal>,
        val mealLogs: List<MealLog>,
        val importedMeals: List<AkImportedMealEntity>,
        val activityOccurrences: List<ActivityOccurrenceEntity>,
        val activities: List<Activity>,
        val activityLogs: List<ActivityLog>
    )

    private fun observeTimeline() {
        Log.d("Meow", "TodayVM observeTimeline()")
        viewModelScope.launch {
            selectedDate
                .flatMapLatest { date ->

                    // ---------- FIRST BRANCH (supplements + meals + mealLogs) ----------
                    val mealBaseFlow =
                        combine(
                            getSupplementOccurrencesForDate(date),
                            getActiveSupplements(),
                            getSupplementDoseLogsForDate(date),
                            getMealOccurrencesForDate(date),
                            getMealsForDate(date)
                        ) { supplementOccurrences, supplements, supplementDoseLogs, mealOccurrences, meals ->

                            Log.d("MEAL_RECON", "VM mealOccurrences input size=${mealOccurrences.size}")
                            mealOccurrences.forEachIndexed { index, occurrence ->
                                Log.d(
                                    "MEAL_RECON",
                                    "VM mealOccurrences#$index > id=${occurrence.id} mealId=${occurrence.mealId} scheduleId=${occurrence.scheduleId} date=${occurrence.date} plannedTimeSeconds=${occurrence.plannedTimeSeconds} sourceType=${occurrence.sourceType} isDeleted=${occurrence.isDeleted}"
                                )
                            }

                            mealOccurrences.groupingBy { it.mealId }
                                .eachCount()
                                .toSortedMap()
                                .forEach { (mealId, count) ->
                                    Log.d("MEAL_RECON", "VM mealOccurrencesByMealId[$mealId]=$count")
                                }

                            Log.d("MEAL_RECON", "VM meals input size=${meals.size}")
                            meals.forEachIndexed { index, meal ->
                                Log.d(
                                    "MEAL_RECON",
                                    "VM meals#$index > id=${meal.id} name=${meal.name} type=${meal.type} isActive=${meal.isActive}"
                                )
                            }

                            meals.groupingBy { it.type }
                                .eachCount()
                                .toSortedMap(compareBy { it.name })
                                .forEach { (type, count) ->
                                    Log.d("MEAL_RECON", "VM mealTemplatesByType[$type]=$count")
                                }

                            MealBaseInputs(
                                supplementOccurrences = supplementOccurrences,
                                supplements = supplements,
                                supplementDoseLogs = supplementDoseLogs,
                                mealOccurrences = mealOccurrences,
                                meals = meals
                            )
                        }

                    val mealWithLogsFlow =
                        combine(
                            mealBaseFlow,
                            mealLogRepository.observeMealLogsForDate(date)
                        ) { base, mealLogs ->

                            Log.d("MEAL_RECON", "VM mealLogs input size=${mealLogs.size}")
                            mealLogs.forEachIndexed { index, log ->
                                Log.d(
                                    "MEAL_RECON",
                                    "VM mealLogs#$index > id=${log.id} occurrenceId=${log.occurrenceId} mealType=${log.mealType} start=${log.start}"
                                )
                            }

                            QuadA(
                                supplementOccurrences = base.supplementOccurrences,
                                supplements = base.supplements,
                                supplementDoseLogs = base.supplementDoseLogs,
                                mealOccurrences = base.mealOccurrences,
                                meals = base.meals,
                                mealLogs = mealLogs
                            )
                        }

                    // ---------- SECOND BRANCH (imported + activities) ----------
                    val activityBranch =
                        combine(
                            getImportedMealsForDate(date),
                            getActivityOccurrencesForDate(date),
                            getActivitiesForDate(date),
                            activityLogRepository.observeActivityLogsForDate(date)
                        ) { importedMeals, activityOccurrences, activities, activityLogs ->

                            Log.d("MEAL_RECON", "VM importedMeals input size=${importedMeals.size}")
                            importedMeals.forEachIndexed { index, meal ->
                                Log.d(
                                    "MEAL_RECON",
                                    "VM importedMeals#$index > groupingKey=${meal.groupingKey} type=${meal.type} timestamp=${meal.timestamp}"
                                )
                            }

                            importedMeals.groupingBy { it.type }
                                .eachCount()
                                .toSortedMap(compareBy { it.name })
                                .forEach { (type, count) ->
                                    Log.d("MEAL_RECON", "VM importedMealsByType[$type]=$count")
                                }

                            QuadB(
                                importedMeals = importedMeals,
                                activityOccurrences = activityOccurrences,
                                activities = activities,
                                activityLogs = activityLogs
                            )
                        }

                    // ---------- FINAL COMBINE ----------
                    combine(mealWithLogsFlow, activityBranch) { a, b ->
                        Log.d(
                            "MEAL_RECON",
                            "VM combine snapshot: mealOccurrences=${a.mealOccurrences.size} meals=${a.meals.size} mealLogs=${a.mealLogs.size} importedMeals=${b.importedMeals.size}"
                        )

                        TimelineInputs(
                            supplementOccurrences = a.supplementOccurrences,
                            supplements = a.supplements,
                            supplementDoseLogs = a.supplementDoseLogs,
                            mealOccurrences = a.mealOccurrences,
                            meals = a.meals,
                            mealLogs = a.mealLogs,
                            importedMeals = b.importedMeals,
                            activityOccurrences = b.activityOccurrences,
                            activities = b.activities,
                            activityLogs = b.activityLogs
                        )
                    }.map { inputs ->
                        Log.d(
                            "MEAL_RECON",
                            "VM before timeline: mealOccurrences=${inputs.mealOccurrences.size} meals=${inputs.meals.size} mealLogs=${inputs.mealLogs.size} imported=${inputs.importedMeals.size}"
                        )

                        buildTodayTimeline(
                            date = date,
                            supplementOccurrences = inputs.supplementOccurrences,
                            supplements = inputs.supplements,
                            supplementDoseLogs = inputs.supplementDoseLogs,
                            mealOccurrences = inputs.mealOccurrences,
                            meals = inputs.meals,
                            mealLogs = inputs.mealLogs,
                            importedMeals = inputs.importedMeals,
                            activityOccurrences = inputs.activityOccurrences,
                            activities = inputs.activities,
                            activityLogs = inputs.activityLogs
                        )
                    }.flowOn(Dispatchers.Default)
                }
                .collect { timeline ->
                    Log.d("Meow", "Timeline update size=${timeline.size}")

                    val mealItems = timeline.filterIsInstance<TimelineItem.MealTimelineItem>()
                    Log.d("MEAL_RECON", "VM final native meal items size=${mealItems.size}")

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
        val mealOccurrences: List<MealOccurrenceEntity>,
        val meals: List<Meal>,
        val mealLogs: List<MealLog>
    )

    private data class QuadB(
        val importedMeals: List<AkImportedMealEntity>,
        val activityOccurrences: List<ActivityOccurrenceEntity>,
        val activities: List<Activity>,
        val activityLogs: List<ActivityLog>
    )

    private data class MealBaseInputs(
        val supplementOccurrences: List<SupplementOccurrenceEntity>,
        val supplements: List<Supplement>,
        val supplementDoseLogs: List<SupplementDoseLog>,
        val mealOccurrences: List<MealOccurrenceEntity>,
        val meals: List<Meal>
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
        Log.d(
            "ACTIVITY_RECON",
            "setExerciseDraft activityId=${draft.activityId} occurrenceId=${draft.occurrenceId} start=${draft.startTime} end=${draft.endTime}"
        )
        _state.update { it.copy(exerciseDraft = draft) }
        persistExerciseDraft(draft)
    }

    private fun clearExerciseDraft() {
        _state.update { it.copy(exerciseDraft = null) }
        clearPersistedExerciseDraft()
    }

    private fun setMealDraft(draft: MealLogInput) {
        Log.d(
            "MEAL_RECON",
            "setMealDraft mealType=${draft.mealType} occurrenceId=${draft.occurrenceId} start=${draft.startTime} end=${draft.endTime}"
        )
        _state.update { it.copy(mealDraft = draft) }
    }

    private fun clearMealDraft() {
        _state.update { it.copy(mealDraft = null) }
    }

    private fun findPlannedMealTime(
        mealType: MealType
    ): LocalTime? =
        state.value.domainTimelineItems
            .filterIsInstance<TimelineItem.MealTimelineItem>()
            .firstOrNull { it.meal.type == mealType }
            ?.time

    private fun restoreExerciseDraftIfPresent() {
        val activityId = savedStateHandle.get<Long?>(ExerciseSavedStateKeys.ACTIVITY_ID)
        val type = savedStateHandle.get<String>(ExerciseSavedStateKeys.TYPE) ?: return
        val logDateIso = savedStateHandle.get<String>(ExerciseSavedStateKeys.LOG_DATE) ?: return
        val startMin = savedStateHandle.get<Int>(ExerciseSavedStateKeys.START_MIN) ?: return
        val endMin = savedStateHandle.get<Int>(ExerciseSavedStateKeys.END_MIN) ?: return

        val notes = savedStateHandle.get<String>(ExerciseSavedStateKeys.NOTES) ?: ""
        val intensity = savedStateHandle.get<Int?>(ExerciseSavedStateKeys.INTENSITY) ?: 0
        val occurrenceId = savedStateHandle.get<String?>(ExerciseSavedStateKeys.OCCURRENCE_ID)
        Log.d(
            "ACTIVITY_RECON",
            "restoreExerciseDraft activityId=$activityId occurrenceId=$occurrenceId"
        )
        val draft = ExerciseDraft(
            activityId = activityId,
            activityType = ActivityType.valueOf(type),
            logDate = LocalDate.parse(logDateIso),
            startTime = minutesToLocalTime(startMin),
            endTime = minutesToLocalTime(endMin),
            notes = notes,
            intensity = intensity,
            occurrenceId = occurrenceId
        )

        _state.update { it.copy(exerciseDraft = draft) }
    }

    private fun persistExerciseDraft(draft: ExerciseDraft) {
        Log.d(
            "ACTIVITY_RECON",
            "persistExerciseDraft activityId=${draft.activityId} occurrenceId=${draft.occurrenceId}"
        )
        savedStateHandle[ExerciseSavedStateKeys.ACTIVITY_ID] = draft.activityId
        savedStateHandle[ExerciseSavedStateKeys.TYPE] = draft.activityType.name
        savedStateHandle[ExerciseSavedStateKeys.LOG_DATE] = draft.logDate.toString()
        savedStateHandle[ExerciseSavedStateKeys.START_MIN] = draft.startTime.toMinutesOfDay()
        savedStateHandle[ExerciseSavedStateKeys.END_MIN] = draft.endTime.toMinutesOfDay()
        savedStateHandle[ExerciseSavedStateKeys.NOTES] = draft.notes
        savedStateHandle[ExerciseSavedStateKeys.INTENSITY] = draft.intensity
        savedStateHandle[ExerciseSavedStateKeys.OCCURRENCE_ID] = draft.occurrenceId
    }

    private fun clearPersistedExerciseDraft() {
        savedStateHandle.remove<Long?>(ExerciseSavedStateKeys.ACTIVITY_ID)
        savedStateHandle.remove<String>(ExerciseSavedStateKeys.TYPE)
        savedStateHandle.remove<String>(ExerciseSavedStateKeys.LOG_DATE)
        savedStateHandle.remove<Int>(ExerciseSavedStateKeys.START_MIN)
        savedStateHandle.remove<Int>(ExerciseSavedStateKeys.END_MIN)
        savedStateHandle.remove<String>(ExerciseSavedStateKeys.NOTES)
        savedStateHandle.remove<Int?>(ExerciseSavedStateKeys.INTENSITY)
        savedStateHandle.remove<String?>(ExerciseSavedStateKeys.OCCURRENCE_ID)
    }

    private fun LocalTime.toMinutesOfDay(): Int = (hour * 60) + minute

    private fun minutesToLocalTime(totalMinutes: Int): LocalTime =
        LocalTime(totalMinutes / 60, totalMinutes % 60)

    private fun nowLocalTime(clock: Clock): LocalTime =
        DomainTimePolicy.nowLocalDateTime(clock).time

    private fun localTimeToEpochMillis(date: LocalDate, time: LocalTime): Long {
        val ldt = LocalDateTime(date = date, time = time)
        return ldt.toInstant(DomainTimePolicy.localTimeZone).toEpochMilliseconds()
    }

    private fun clampEndTimeNotBeforeStart(start: LocalTime, endCandidate: LocalTime): LocalTime =
        if (endCandidate < start) start else endCandidate

    private fun addDefaultDuration(start: LocalTime): LocalTime {
        val startMinutes = start.toMinutesOfDay()
        val cappedEndMinutes = (startMinutes + 60).coerceAtMost((23 * 60) + 59)
        return minutesToLocalTime(cappedEndMinutes)
    }
}