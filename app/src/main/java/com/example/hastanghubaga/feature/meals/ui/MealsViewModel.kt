package com.example.hastanghubaga.feature.meals.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hastanghubaga.data.local.dao.meal.MealScheduleDao
import com.example.hastanghubaga.data.local.entity.meal.MealEntity
import com.example.hastanghubaga.data.local.entity.meal.MealNutritionEntity
import com.example.hastanghubaga.data.local.entity.meal.MealScheduleAnchoredTimeEntity
import com.example.hastanghubaga.data.local.entity.meal.MealScheduleEntity
import com.example.hastanghubaga.data.local.entity.meal.MealScheduleFixedTimeEntity
import com.example.hastanghubaga.data.local.entity.meal.MealScheduleWithTimes
import com.example.hastanghubaga.data.local.entity.meal.MealType
import com.example.hastanghubaga.domain.repository.meal.MealRepository
import com.example.hastanghubaga.domain.usecase.meal.MaterializeMealOccurrencesForDateUseCase
import com.example.hastanghubaga.feature.schedule.ui.model.AnchorTypeUi
import com.example.hastanghubaga.feature.schedule.ui.model.AnchoredTimeRowUi
import com.example.hastanghubaga.feature.schedule.ui.model.FixedTimeRowUi
import com.example.hastanghubaga.feature.schedule.ui.model.RecurrenceMode
import com.example.hastanghubaga.feature.schedule.ui.model.ScheduleEditorState
import com.example.hastanghubaga.feature.schedule.ui.model.ScheduleValidationError
import com.example.hastanghubaga.feature.schedule.ui.model.TimingMode
import com.example.hastanghubaga.feature.schedule.ui.model.WeekdayUi
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

data class MealsUiState(
    val items: List<MealListItemUi> = emptyList(),
    val editor: MealEditorUiState? = null,
    val schedule: ScheduleEditorState = defaultMealScheduleEditorState()
)

data class MealListItemUi(
    val id: Long,
    val name: String,
    val typeLabel: String,
    val treatAsLabel: String?,
    val isActive: Boolean,
    val hasSchedule: Boolean,
    val notes: String?
)

data class MealEditorUiState(
    val id: Long? = null,
    val name: String = "",
    val type: MealType = MealType.BREAKFAST,
    val treatAsAnchor: MealType? = null,
    val isActive: Boolean = true,
    val notes: String = "",
    val isNew: Boolean = true
)

@HiltViewModel
class MealsViewModel @Inject constructor(
    private val mealRepository: MealRepository,
    private val mealScheduleDao: MealScheduleDao,
    private val materializeMealOccurrencesForDateUseCase: MaterializeMealOccurrencesForDateUseCase
) : ViewModel() {

    private val editorState = MutableStateFlow<MealEditorUiState?>(null)
    private val scheduleState = MutableStateFlow(defaultMealScheduleEditorState())

    val state: StateFlow<MealsUiState> =
        combine(
            mealRepository.observeAll(),
            mealScheduleDao.observeAllSchedules(),
            editorState,
            scheduleState
        ) { meals, schedules, editor, schedule ->
            val scheduleMealIds = schedules.map { it.schedule.mealId }.toSet()

            val items = meals.map { meal ->
                MealListItemUi(
                    id = meal.id,
                    name = meal.name,
                    typeLabel = meal.type.toDisplayLabel(),
                    treatAsLabel = meal.treatAsAnchor?.let { "Treat as ${it.toDisplayLabel()}" },
                    isActive = meal.isActive,
                    hasSchedule = meal.id in scheduleMealIds,
                    notes = meal.notes
                )
            }

            MealsUiState(
                items = items,
                editor = editor,
                schedule = schedule
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MealsUiState()
        )

    fun onAddClick() {
        editorState.value = MealEditorUiState(
            isNew = true
        )
        scheduleState.value = defaultMealScheduleEditorState()
    }

    fun onEditClick(id: Long) {
        viewModelScope.launch {
            val meal = mealRepository.getMealById(id) ?: return@launch
            val schedule = mealScheduleDao.getScheduleForMeal(id)

            editorState.value = MealEditorUiState(
                id = meal.id,
                name = meal.name,
                type = meal.type,
                treatAsAnchor = meal.treatAsAnchor,
                isActive = meal.isActive,
                notes = meal.notes.orEmpty(),
                isNew = false
            )

            scheduleState.value = schedule?.toEditorState() ?: defaultMealScheduleEditorState()
        }
    }

    fun onNameChanged(value: String) {
        editorState.update { current -> current?.copy(name = value) }
    }

    fun onTypeChanged(value: MealType) {
        editorState.update { current -> current?.copy(type = value) }
    }

    fun onTreatAsAnchorChanged(value: MealType?) {
        editorState.update { current -> current?.copy(treatAsAnchor = value) }
    }

    fun onIsActiveChanged(value: Boolean) {
        editorState.update { current -> current?.copy(isActive = value) }
    }

    fun onNotesChanged(value: String) {
        editorState.update { current -> current?.copy(notes = value) }
    }

    fun onEnabledChanged(value: Boolean) {
        scheduleState.updateAndValidate { it.copy(isEnabled = value) }
    }

    fun onRecurrenceModeChanged(value: RecurrenceMode) {
        scheduleState.updateAndValidate {
            it.copy(
                recurrenceMode = value,
                selectedWeekdays = if (value == RecurrenceMode.WEEKLY) {
                    it.selectedWeekdays
                } else {
                    emptySet()
                }
            )
        }
    }

    fun onIntervalInputChanged(value: String) {
        if (value.all(Char::isDigit)) {
            scheduleState.updateAndValidate { it.copy(intervalInput = value) }
        }
    }

    fun onWeekdayToggled(value: WeekdayUi) {
        scheduleState.updateAndValidate { current ->
            val updated = current.selectedWeekdays.toMutableSet()
            if (!updated.add(value)) updated.remove(value)
            current.copy(selectedWeekdays = updated)
        }
    }

    fun onStartDateClick() {
        scheduleState.updateAndValidate { current ->
            current.copy(
                startDate = current.startDate ?: Clock.System.todayIn(TimeZone.currentSystemDefault())
            )
        }
    }

    fun onEndDateToggleChanged(value: Boolean) {
        scheduleState.updateAndValidate { current ->
            current.copy(
                hasEndDate = value,
                endDate = if (value) {
                    current.endDate ?: current.startDate ?: Clock.System.todayIn(TimeZone.currentSystemDefault())
                } else {
                    null
                }
            )
        }
    }

    fun onEndDateClick() {
        scheduleState.updateAndValidate { current ->
            if (!current.hasEndDate) current
            else current.copy(
                endDate = current.endDate ?: current.startDate ?: Clock.System.todayIn(TimeZone.currentSystemDefault())
            )
        }
    }

    fun onTimingModeChanged(value: TimingMode) {
        scheduleState.updateAndValidate { it.copy(timingMode = value) }
    }

    fun onFixedTimeChanged(rowId: String, value: String) {
        scheduleState.updateAndValidate { current ->
            current.copy(
                fixedTimes = current.fixedTimes.map { row ->
                    if (row.id == rowId) row.copy(timeInput = value) else row
                }
            )
        }
    }

    fun onAddFixedTime() {
        scheduleState.updateAndValidate { current ->
            current.copy(
                fixedTimes = current.fixedTimes + FixedTimeRowUi(
                    id = nextRowId(),
                    timeInput = ""
                )
            )
        }
    }

    fun onRemoveFixedTime(rowId: String) {
        scheduleState.updateAndValidate { current ->
            val remaining = current.fixedTimes.filterNot { it.id == rowId }
            current.copy(
                fixedTimes = if (remaining.isEmpty()) {
                    listOf(FixedTimeRowUi(id = nextRowId(), timeInput = ""))
                } else {
                    remaining
                }
            )
        }
    }

    fun onAnchoredRowAnchorChanged(rowId: String, anchor: AnchorTypeUi) {
        scheduleState.updateAndValidate { current ->
            current.copy(
                anchoredTimes = current.anchoredTimes.map { row ->
                    if (row.id == rowId) row.copy(anchor = anchor) else row
                }
            )
        }
    }

    fun onAnchoredRowOffsetChanged(rowId: String, value: String) {
        if (value.isEmpty() || value == "-" || value.all(Char::isDigit) || (value.startsWith("-") && value.drop(1).all(Char::isDigit))) {
            scheduleState.updateAndValidate { current ->
                current.copy(
                    anchoredTimes = current.anchoredTimes.map { row ->
                        if (row.id == rowId) row.copy(offsetMinutesInput = value) else row
                    }
                )
            }
        }
    }

    fun onAddAnchoredRow() {
        scheduleState.updateAndValidate { current ->
            current.copy(
                anchoredTimes = current.anchoredTimes + AnchoredTimeRowUi(
                    id = nextRowId(),
                    anchor = AnchorTypeUi.BREAKFAST,
                    offsetMinutesInput = "0"
                )
            )
        }
    }

    fun onRemoveAnchoredRow(rowId: String) {
        scheduleState.updateAndValidate { current ->
            val remaining = current.anchoredTimes.filterNot { it.id == rowId }
            current.copy(
                anchoredTimes = if (remaining.isEmpty()) {
                    listOf(
                        AnchoredTimeRowUi(
                            id = nextRowId(),
                            anchor = AnchorTypeUi.BREAKFAST,
                            offsetMinutesInput = "0"
                        )
                    )
                } else {
                    remaining
                }
            )
        }
    }

    fun onDismissEditor() {
        editorState.value = null
        scheduleState.value = defaultMealScheduleEditorState()
    }

    fun onSaveClick() {
        val editor = editorState.value ?: return
        val validatedSchedule = scheduleState.value.validate()

        if (validatedSchedule.validationErrors.isNotEmpty()) {
            scheduleState.value = validatedSchedule
            return
        }

        viewModelScope.launch {
            val trimmedName = editor.name.trim()
            if (trimmedName.isBlank()) return@launch

            val savedMealId = mealRepository.upsertMeal(
                meal = MealEntity(
                    id = editor.id ?: 0L,
                    name = trimmedName,
                    type = editor.type,
                    treatAsAnchor = editor.treatAsAnchor,
                    isActive = editor.isActive
                ),
                nutrition = MealNutritionEntity(
                    mealId = editor.id ?: 0L,
                    calories = 0,
                    protein = 0.0,
                    carbs = 0.0,
                    fat = 0.0
                )
            )

            if (!validatedSchedule.isEnabled) {
                mealScheduleDao.deleteFullScheduleForMeal(savedMealId)
            } else {
                val scheduleEntity = MealScheduleEntity(
                    id = mealScheduleDao.getScheduleIdForMeal(savedMealId) ?: 0L,
                    mealId = savedMealId,
                    recurrenceType = validatedSchedule.toRecurrenceTypeString(),
                    interval = validatedSchedule.intervalInput.toIntOrNull()?.coerceAtLeast(1) ?: 1,
                    weeklyDays = validatedSchedule.toWeeklyDaysString(),
                    startDate = validatedSchedule.startDate?.toString()
                        ?: Clock.System.todayIn(TimeZone.currentSystemDefault()).toString(),
                    endDate = if (validatedSchedule.hasEndDate) validatedSchedule.endDate?.toString() else null,
                    timingType = validatedSchedule.toTimingTypeString(),
                    isEnabled = true
                )

                when (validatedSchedule.timingMode) {
                    TimingMode.FIXED -> {
                        mealScheduleDao.replaceWithFixedTimes(
                            schedule = scheduleEntity,
                            fixedTimes = validatedSchedule.fixedTimes.map { row ->
                                MealScheduleFixedTimeEntity(
                                    scheduleId = 0L,
                                    time = row.timeInput
                                )
                            }
                        )
                    }

                    TimingMode.ANCHORED -> {
                        mealScheduleDao.replaceWithAnchoredTimes(
                            schedule = scheduleEntity,
                            anchoredTimes = validatedSchedule.anchoredTimes.map { row ->
                                MealScheduleAnchoredTimeEntity(
                                    scheduleId = 0L,
                                    anchorType = row.anchor.toDbAnchorType(),
                                    offsetMinutes = row.offsetMinutesInput.toIntOrNull() ?: 0
                                )
                            }
                        )
                    }
                }
            }

            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            materializeMealOccurrencesForDateUseCase(today)

            editorState.value = null
            scheduleState.value = defaultMealScheduleEditorState()
        }
    }

    fun onDeleteClick() {
        val editor = editorState.value ?: return
        val id = editor.id ?: return

        viewModelScope.launch {
            mealRepository.deleteMealById(id)
            editorState.value = null
            scheduleState.value = defaultMealScheduleEditorState()
        }
    }

    private fun MutableStateFlow<ScheduleEditorState>.updateAndValidate(
        transform: (ScheduleEditorState) -> ScheduleEditorState
    ) {
        update { current -> transform(current).validate() }
    }
}

private fun defaultMealScheduleEditorState(): ScheduleEditorState {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val initial = ScheduleEditorState(
        isEnabled = true,
        recurrenceMode = RecurrenceMode.DAILY,
        intervalInput = "1",
        selectedWeekdays = emptySet(),
        startDate = today,
        hasEndDate = false,
        endDate = null,
        timingMode = TimingMode.FIXED,
        fixedTimes = listOf(
            FixedTimeRowUi(
                id = nextRowId(),
                timeInput = ""
            )
        ),
        anchoredTimes = listOf(
            AnchoredTimeRowUi(
                id = nextRowId(),
                anchor = AnchorTypeUi.BREAKFAST,
                offsetMinutesInput = "0"
            )
        ),
        validationErrors = emptyList(),
        summaryText = ""
    )
    return initial.validate()
}

private fun MealScheduleWithTimes.toEditorState(): ScheduleEditorState {
    val recurrenceMode =
        when (schedule.recurrenceType) {
            "WEEKLY" -> RecurrenceMode.WEEKLY
            else -> RecurrenceMode.DAILY
        }

    val timingMode =
        when (schedule.timingType) {
            "ANCHORED" -> TimingMode.ANCHORED
            else -> TimingMode.FIXED
        }

    val fixedRows =
        if (fixedTimes.isNotEmpty()) {
            fixedTimes.map { row ->
                FixedTimeRowUi(
                    id = nextRowId(),
                    timeInput = row.time
                )
            }
        } else {
            listOf(FixedTimeRowUi(id = nextRowId(), timeInput = ""))
        }

    val anchoredRows =
        if (anchoredTimes.isNotEmpty()) {
            anchoredTimes.map { row ->
                AnchoredTimeRowUi(
                    id = nextRowId(),
                    anchor = row.anchorType.toUiAnchorType(),
                    offsetMinutesInput = row.offsetMinutes.toString()
                )
            }
        } else {
            listOf(
                AnchoredTimeRowUi(
                    id = nextRowId(),
                    anchor = AnchorTypeUi.BREAKFAST,
                    offsetMinutesInput = "0"
                )
            )
        }

    return ScheduleEditorState(
        isEnabled = schedule.isEnabled,
        recurrenceMode = recurrenceMode,
        intervalInput = schedule.interval.toString(),
        selectedWeekdays = schedule.weeklyDays.toWeekdayUiSet(),
        startDate = LocalDate.parse(schedule.startDate),
        hasEndDate = schedule.endDate != null,
        endDate = schedule.endDate?.let(LocalDate::parse),
        timingMode = timingMode,
        fixedTimes = fixedRows,
        anchoredTimes = anchoredRows,
        validationErrors = emptyList(),
        summaryText = ""
    ).validate()
}

private fun ScheduleEditorState.validate(): ScheduleEditorState {
    val errors = buildList {
        if (startDate == null) {
            add(ScheduleValidationError.MissingStartDate)
        }

        val parsedInterval = intervalInput.toIntOrNull()
        if (parsedInterval == null || parsedInterval <= 0) {
            add(ScheduleValidationError.InvalidInterval)
        }

        if (recurrenceMode == RecurrenceMode.WEEKLY && selectedWeekdays.isEmpty()) {
            add(ScheduleValidationError.NoWeekdaysSelected)
        }

        if (hasEndDate && startDate != null && endDate != null && endDate < startDate) {
            add(ScheduleValidationError.EndDateBeforeStartDate)
        }

        when (timingMode) {
            TimingMode.FIXED -> {
                if (fixedTimes.isEmpty()) {
                    add(ScheduleValidationError.NoTimesDefined)
                }

                val hasInvalid =
                    fixedTimes.any { !isValidFixedTime(it.timeInput) }

                if (hasInvalid) {
                    add(ScheduleValidationError.InvalidTimeFormat(""))
                }
            }

            TimingMode.ANCHORED -> {
                if (anchoredTimes.isEmpty()) {
                    add(ScheduleValidationError.NoTimesDefined)
                }

                val hasInvalid =
                    anchoredTimes.any { it.offsetMinutesInput.toIntOrNull() == null }

                if (hasInvalid) {
                    add(ScheduleValidationError.InvalidOffset(""))
                }
            }
        }
    }

    return copy(
        validationErrors = errors,
        summaryText = buildMealScheduleSummary(this, errors)
    )
}

private fun buildMealScheduleSummary(
    state: ScheduleEditorState,
    errors: List<ScheduleValidationError>
): String {
    if (!state.isEnabled) return "Schedule disabled."
    if (errors.isNotEmpty()) return "Fix validation issues to save this meal schedule."

    val recurrenceText =
        when (state.recurrenceMode) {
            RecurrenceMode.DAILY -> {
                val n = state.intervalInput.toIntOrNull() ?: 1
                if (n == 1) "Daily" else "Every $n days"
            }

            RecurrenceMode.WEEKLY -> {
                val n = state.intervalInput.toIntOrNull() ?: 1
                val days = WeekdayUi.entries
                    .filter { it in state.selectedWeekdays }
                    .joinToString(", ") { it.shortLabel() }
                if (n == 1) {
                    "Weekly on $days"
                } else {
                    "Every $n weeks on $days"
                }
            }
        }

    val timingText =
        when (state.timingMode) {
            TimingMode.FIXED -> {
                val times = state.fixedTimes.joinToString(", ") { it.timeInput }
                "Fixed at $times"
            }

            TimingMode.ANCHORED -> {
                val rows = state.anchoredTimes.joinToString(", ") {
                    "${it.anchor.label()} ${formatOffset(it.offsetMinutesInput)}"
                }
                "Anchored at $rows"
            }
        }

    val dateText =
        buildString {
            append("Start ${state.startDate}")
            if (state.hasEndDate) append(", end ${state.endDate}")
        }

    return "$recurrenceText • $timingText • $dateText"
}

private fun ScheduleEditorState.toRecurrenceTypeString(): String =
    when (recurrenceMode) {
        RecurrenceMode.DAILY -> "DAILY"
        RecurrenceMode.WEEKLY -> "WEEKLY"
    }

private fun ScheduleEditorState.toTimingTypeString(): String =
    when (timingMode) {
        TimingMode.FIXED -> "FIXED_TIMES"
        TimingMode.ANCHORED -> "ANCHORED"
    }

private fun ScheduleEditorState.toWeeklyDaysString(): String? {
    if (recurrenceMode != RecurrenceMode.WEEKLY) return null
    return WeekdayUi.entries
        .filter { it in selectedWeekdays }
        .joinToString(",") { it.name }
        .ifBlank { null }
}

private fun String?.toWeekdayUiSet(): Set<WeekdayUi> {
    if (this.isNullOrBlank()) return emptySet()
    return split(",")
        .mapNotNull { raw ->
            WeekdayUi.entries.firstOrNull { it.name == raw.trim() }
        }
        .toSet()
}

private fun String.toUiAnchorType(): AnchorTypeUi =
    AnchorTypeUi.entries.firstOrNull { it.name == this } ?: AnchorTypeUi.BREAKFAST

private fun AnchorTypeUi.toDbAnchorType(): String = name

private fun isValidFixedTime(value: String): Boolean {
    val regex = Regex("""^([01]\d|2[0-3]):([0-5]\d)$""")
    return regex.matches(value)
}

private fun formatOffset(value: String): String {
    val parsed = value.toIntOrNull() ?: 0
    return when {
        parsed == 0 -> "(at anchor)"
        parsed > 0 -> "(+$parsed min)"
        else -> "($parsed min)"
    }
}

private fun MealType.toDisplayLabel(): String =
    name
        .lowercase()
        .split("_")
        .joinToString(" ") { word -> word.replaceFirstChar { it.uppercase() } }

private fun WeekdayUi.shortLabel(): String =
    when (this) {
        WeekdayUi.MONDAY -> "Mon"
        WeekdayUi.TUESDAY -> "Tue"
        WeekdayUi.WEDNESDAY -> "Wed"
        WeekdayUi.THURSDAY -> "Thu"
        WeekdayUi.FRIDAY -> "Fri"
        WeekdayUi.SATURDAY -> "Sat"
        WeekdayUi.SUNDAY -> "Sun"
    }

private fun AnchorTypeUi.label(): String =
    when (this) {
        AnchorTypeUi.WAKE_UP -> "Wake Up"
        AnchorTypeUi.BREAKFAST -> "Breakfast"
        AnchorTypeUi.LUNCH -> "Lunch"
        AnchorTypeUi.DINNER -> "Dinner"
        AnchorTypeUi.SLEEP -> "Sleep"
        AnchorTypeUi.BEFORE_WORKOUT -> "Pre workout"
        AnchorTypeUi.DURING_WORKOUT -> "Workout"
        AnchorTypeUi.AFTER_WORKOUT -> "Post workout"
        AnchorTypeUi.MIDNIGHT -> "Midnight"
        AnchorTypeUi.SNACK -> "Snack"
    }

private var nextGeneratedRowId: Long = 0L

private fun nextRowId(): String {
    nextGeneratedRowId += 1L
    return "meal_schedule_row_$nextGeneratedRowId"
}