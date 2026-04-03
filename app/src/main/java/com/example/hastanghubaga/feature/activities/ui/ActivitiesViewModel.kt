package com.example.hastanghubaga.feature.activities.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hastanghubaga.data.local.dao.activity.ActivityEntityDao
import com.example.hastanghubaga.data.local.dao.activity.ActivityScheduleDao
import com.example.hastanghubaga.data.local.dao.activity.ActivityScheduleWriteModel
import com.example.hastanghubaga.data.local.entity.activity.ActivityEntity
import com.example.hastanghubaga.data.local.entity.activity.ActivityScheduleAnchoredTimeEntity
import com.example.hastanghubaga.data.local.entity.activity.ActivityScheduleEntity
import com.example.hastanghubaga.data.local.entity.activity.ActivityScheduleFixedTimeEntity
import com.example.hastanghubaga.data.local.entity.supplement.ScheduleRecurrenceType
import com.example.hastanghubaga.data.local.entity.supplement.ScheduleTimingType
import com.example.hastanghubaga.domain.model.activity.ActivityType
import com.example.hastanghubaga.domain.schedule.model.TimeAnchor
import com.example.hastanghubaga.domain.time.DomainTimePolicy
import com.example.hastanghubaga.domain.usecase.activity.MaterializeActivityOccurrencesForDateUseCase
import com.example.hastanghubaga.feature.schedule.ui.model.AnchorTypeUi
import com.example.hastanghubaga.feature.schedule.ui.model.AnchoredTimeRowUi
import com.example.hastanghubaga.feature.schedule.ui.model.FixedTimeRowUi
import com.example.hastanghubaga.feature.schedule.ui.model.ParseResult
import com.example.hastanghubaga.feature.schedule.ui.model.ParsedAnchoredTimeRow
import com.example.hastanghubaga.feature.schedule.ui.model.ParsedFixedTimeRow
import com.example.hastanghubaga.feature.schedule.ui.model.ParsedScheduleEditorDraft
import com.example.hastanghubaga.feature.schedule.ui.model.RecurrenceMode
import com.example.hastanghubaga.feature.schedule.ui.model.ScheduleEditorAction
import com.example.hastanghubaga.feature.schedule.ui.model.ScheduleEditorMapper
import com.example.hastanghubaga.feature.schedule.ui.model.ScheduleEditorReducer
import com.example.hastanghubaga.feature.schedule.ui.model.ScheduleEditorState
import com.example.hastanghubaga.feature.schedule.ui.model.ScheduleValidationError
import com.example.hastanghubaga.feature.schedule.ui.model.TimingMode
import com.example.hastanghubaga.feature.schedule.ui.model.WeekdayUi
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DayOfWeek as KtxDayOfWeek
import kotlinx.datetime.LocalTime as KtxLocalTime

data class ActivitiesUiState(
    val items: List<ActivityListItemUi> = emptyList(),
    val editor: ActivityEditorUiState? = null
)

data class ActivityEditorUiState(
    val id: Long? = null,
    val type: ActivityType = ActivityType.OTHER,
    val notes: String = "",
    val intensity: String = "",
    val isWorkout: Boolean = false,
    val isNew: Boolean = true,
    val scheduleEditors: List<ScheduleEditorState> = listOf(
        ScheduleEditorReducer.initialState()
    ),
    val scheduleSaveErrors: List<String> = emptyList()
)

@HiltViewModel
class ActivitiesViewModel @Inject constructor(
    private val activityEntityDao: ActivityEntityDao,
    private val activityScheduleDao: ActivityScheduleDao,
    private val materializeActivityOccurrencesForDateUseCase: MaterializeActivityOccurrencesForDateUseCase
) : ViewModel() {

    private val editorState = MutableStateFlow<ActivityEditorUiState?>(null)

    private val timeFormatter = DateTimeFormatter.ofPattern("MMM d, h:mm a")
    private val zoneId = ZoneId.systemDefault()

    private val itemsFlow =
        activityEntityDao
            .observeAllActivities()
            .map { activities ->
                activities.map { activity ->
                    val schedules = activityScheduleDao.getSchedulesForActivity(activity.id)
                    val isScheduled = schedules.any { it.isEnabled }

                    ActivityListItemUi(
                        id = activity.id,
                        typeLabel = activity.type.toDisplayLabel(),
                        notes = activity.notes,
                        intensityLabel = buildIntensityLabel(activity, isScheduled),
                        startLabel = formatTimestamp(activity.startTimestamp)
                    )
                }
            }

    val state: StateFlow<ActivitiesUiState> =
        combine(
            itemsFlow,
            editorState
        ) { items, editor ->
            ActivitiesUiState(
                items = items,
                editor = editor
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ActivitiesUiState()
        )

    fun onAddClick() {
        editorState.value = ActivityEditorUiState(
            isNew = true,
            scheduleEditors = listOf(ScheduleEditorReducer.initialState())
        )
    }

    fun onEditClick(id: Long) {
        viewModelScope.launch {
            val activity = activityEntityDao.getActivityById(id) ?: return@launch
            val persistedSchedules = activityScheduleDao.getSchedulesForActivity(id)

            val mappedScheduleEditors = if (persistedSchedules.isEmpty()) {
                listOf(ScheduleEditorReducer.initialState())
            } else {
                persistedSchedules.map { schedule ->
                    val fixedTimes = activityScheduleDao.getFixedTimesForSchedule(schedule.id)
                    val anchoredTimes = activityScheduleDao.getAnchoredTimesForSchedule(schedule.id)
                    persistedScheduleToEditorState(
                        schedule = schedule,
                        fixedTimes = fixedTimes,
                        anchoredTimes = anchoredTimes
                    )
                }
            }

            editorState.value = ActivityEditorUiState(
                id = activity.id,
                type = activity.type,
                notes = activity.notes.orEmpty(),
                intensity = activity.intensity?.toString().orEmpty(),
                isWorkout = activity.isWorkout,
                isNew = false,
                scheduleEditors = mappedScheduleEditors,
                scheduleSaveErrors = emptyList()
            )
        }
    }

    fun onTypeChanged(value: ActivityType) {
        editorState.update { current -> current?.copy(type = value) }
    }

    fun onNotesChanged(value: String) {
        editorState.update { current -> current?.copy(notes = value) }
    }

    fun onIntensityChanged(value: String) {
        editorState.update { current -> current?.copy(intensity = value) }
    }

    fun onIsWorkoutChanged(value: Boolean) {
        editorState.update { current -> current?.copy(isWorkout = value) }
    }

    fun onAddScheduleClick() {
        editorState.update { current ->
            current?.copy(
                scheduleEditors = current.scheduleEditors + ScheduleEditorReducer.initialState(),
                scheduleSaveErrors = emptyList()
            )
        }
    }

    fun onRemoveScheduleClick(index: Int) {
        editorState.update { current ->
            current?.copy(
                scheduleEditors = current.scheduleEditors
                    .filterIndexed { i, _ -> i != index }
                    .ifEmpty { listOf(ScheduleEditorReducer.initialState()) },
                scheduleSaveErrors = emptyList()
            )
        }
    }

    fun onScheduleAction(index: Int, action: ScheduleEditorAction) {
        editorState.update { current ->
            current ?: return@update null

            val updatedSchedules = current.scheduleEditors.mapIndexed { i, scheduleState ->
                if (i == index) {
                    ScheduleEditorReducer.reduce(scheduleState, action)
                } else {
                    scheduleState
                }
            }

            current.copy(
                scheduleEditors = updatedSchedules,
                scheduleSaveErrors = emptyList()
            )
        }
    }

    fun onDismissEditor() {
        editorState.value = null
    }

    fun onSaveClick() {
        val editor = editorState.value ?: return

        val parsedSchedules = editor.scheduleEditors.map { ScheduleEditorMapper.parse(it) }
        val invalidSchedulesExist = parsedSchedules.any { it is ParseResult.Invalid }

        if (invalidSchedulesExist) {
            editorState.update { current ->
                current?.copy(
                    scheduleEditors = current.scheduleEditors.map { ScheduleEditorMapper.validate(it) },
                    scheduleSaveErrors = buildScheduleSaveErrors(parsedSchedules)
                )
            }
            return
        }

        val validParsedSchedules = parsedSchedules.mapNotNull {
            (it as? ParseResult.Valid)?.draft
        }

        viewModelScope.launch {
            val parsedIntensity = editor.intensity.trim().toIntOrNull()

            val activityId = if (editor.isNew) {
                activityEntityDao.insertActivity(
                    ActivityEntity(
                        type = editor.type,
                        startTimestamp = System.currentTimeMillis(),
                        endTimestamp = null,
                        notes = editor.notes.trim().ifBlank { null },
                        intensity = parsedIntensity,
                        isWorkout = editor.isWorkout
                    )
                )
            } else {
                val existing = editor.id?.let { activityEntityDao.getActivityById(it) }
                    ?: return@launch

                activityEntityDao.updateActivity(
                    existing.copy(
                        type = editor.type,
                        notes = editor.notes.trim().ifBlank { null },
                        intensity = parsedIntensity,
                        isWorkout = editor.isWorkout
                    )
                )
                existing.id
            }

            val writeModels = validParsedSchedules.map { draft ->
                parsedDraftToWriteModel(
                    activityId = activityId,
                    draft = draft
                )
            }

            activityScheduleDao.replaceSchedulesForActivity(
                activityId = activityId,
                schedules = writeModels
            )

            materializeActivityOccurrencesForDateUseCase(
                date = DomainTimePolicy.todayLocal()
            )

            editorState.value = null
        }
    }

    fun onDeleteClick() {
        val editor = editorState.value ?: return
        val id = editor.id ?: return

        viewModelScope.launch {
            val existing = activityEntityDao.getActivityById(id) ?: return@launch
            activityEntityDao.deleteActivity(existing)
            materializeActivityOccurrencesForDateUseCase(
                date = DomainTimePolicy.todayLocal()
            )
            editorState.value = null
        }
    }

    private fun persistedScheduleToEditorState(
        schedule: ActivityScheduleEntity,
        fixedTimes: List<ActivityScheduleFixedTimeEntity>,
        anchoredTimes: List<ActivityScheduleAnchoredTimeEntity>
    ): ScheduleEditorState {
        val recurrenceMode = when (schedule.recurrenceType) {
            ScheduleRecurrenceType.DAILY -> RecurrenceMode.DAILY
            ScheduleRecurrenceType.WEEKLY -> RecurrenceMode.WEEKLY
        }

        val timingMode = when (schedule.timingType) {
            ScheduleTimingType.FIXED -> TimingMode.FIXED
            ScheduleTimingType.ANCHORED -> TimingMode.ANCHORED
        }

        val state = ScheduleEditorState(
            isEnabled = schedule.isEnabled,
            recurrenceMode = recurrenceMode,
            intervalInput = schedule.interval.toString(),
            selectedWeekdays = schedule.weeklyDays
                ?.map(::ktxDayOfWeekToUi)
                ?.toSet()
                .orEmpty(),
            startDate = schedule.startDate,
            hasEndDate = schedule.endDate != null,
            endDate = schedule.endDate,
            timingMode = timingMode,
            fixedTimes = fixedTimes.map { fixed ->
                FixedTimeRowUi(
                    id = "persisted_fixed_${fixed.id}",
                    timeInput = formatLocalTime(fixed.time)
                )
            },
            anchoredTimes = anchoredTimes.map { anchored ->
                AnchoredTimeRowUi(
                    id = "persisted_anchor_${anchored.id}",
                    anchor = timeAnchorToUi(anchored.anchor),
                    offsetMinutesInput = anchored.offsetMinutes.toString()
                )
            }
        )

        val normalized = when (timingMode) {
            TimingMode.FIXED -> if (state.fixedTimes.isEmpty()) {
                state.copy(fixedTimes = listOf(ScheduleEditorMapper.newFixedTimeRow()))
            } else {
                state
            }

            TimingMode.ANCHORED -> if (state.anchoredTimes.isEmpty()) {
                state.copy(anchoredTimes = listOf(ScheduleEditorMapper.newAnchoredTimeRow()))
            } else {
                state
            }
        }

        return ScheduleEditorMapper.validate(normalized)
    }

    private fun parsedDraftToWriteModel(
        activityId: Long,
        draft: ParsedScheduleEditorDraft
    ): ActivityScheduleWriteModel {
        val scheduleEntity = ActivityScheduleEntity(
            activityId = activityId,
            recurrenceType = when (draft.recurrenceMode) {
                RecurrenceMode.DAILY -> ScheduleRecurrenceType.DAILY
                RecurrenceMode.WEEKLY -> ScheduleRecurrenceType.WEEKLY
            },
            interval = draft.interval,
            weeklyDays = when (draft.recurrenceMode) {
                RecurrenceMode.DAILY -> null
                RecurrenceMode.WEEKLY -> draft.selectedWeekdays.map(::uiWeekdayToKtx)
            },
            startDate = draft.startDate,
            endDate = if (draft.hasEndDate) draft.endDate else null,
            timingType = when (draft.timingMode) {
                TimingMode.FIXED -> ScheduleTimingType.FIXED
                TimingMode.ANCHORED -> ScheduleTimingType.ANCHORED
            },
            isEnabled = draft.isEnabled
        )

        val fixedTimes = when (draft.timingMode) {
            TimingMode.FIXED -> draft.fixedTimes.mapIndexed { index, row ->
                parsedFixedTimeToEntity(
                    row = row,
                    sortOrder = index
                )
            }

            TimingMode.ANCHORED -> emptyList()
        }

        val anchoredTimes = when (draft.timingMode) {
            TimingMode.FIXED -> emptyList()
            TimingMode.ANCHORED -> draft.anchoredTimes.mapIndexed { index, row ->
                parsedAnchoredTimeToEntity(
                    row = row,
                    sortOrder = index
                )
            }
        }

        return ActivityScheduleWriteModel(
            schedule = scheduleEntity,
            fixedTimes = fixedTimes,
            anchoredTimes = anchoredTimes
        )
    }

    private fun parsedFixedTimeToEntity(
        row: ParsedFixedTimeRow,
        sortOrder: Int
    ): ActivityScheduleFixedTimeEntity {
        return ActivityScheduleFixedTimeEntity(
            scheduleId = 0L,
            time = KtxLocalTime(
                hour = row.hour,
                minute = row.minute
            ),
            label = null,
            sortOrder = sortOrder
        )
    }

    private fun parsedAnchoredTimeToEntity(
        row: ParsedAnchoredTimeRow,
        sortOrder: Int
    ): ActivityScheduleAnchoredTimeEntity {
        return ActivityScheduleAnchoredTimeEntity(
            scheduleId = 0L,
            anchor = uiAnchorToTimeAnchor(row.anchor),
            offsetMinutes = row.offsetMinutes,
            label = null,
            sortOrder = sortOrder
        )
    }

    private fun uiWeekdayToKtx(value: WeekdayUi): KtxDayOfWeek {
        return KtxDayOfWeek.valueOf(value.name)
    }

    private fun ktxDayOfWeekToUi(value: KtxDayOfWeek): WeekdayUi {
        return WeekdayUi.valueOf(value.name)
    }

    private fun uiAnchorToTimeAnchor(value: AnchorTypeUi): TimeAnchor {
        return when (value) {
            AnchorTypeUi.WAKE_UP -> TimeAnchor.WAKEUP
            AnchorTypeUi.BREAKFAST -> TimeAnchor.BREAKFAST
            AnchorTypeUi.LUNCH -> TimeAnchor.LUNCH
            AnchorTypeUi.DINNER -> TimeAnchor.DINNER
            AnchorTypeUi.SLEEP -> TimeAnchor.SLEEP
            AnchorTypeUi.BEFORE_WORKOUT -> TimeAnchor.BEFORE_WORKOUT
            AnchorTypeUi.DURING_WORKOUT -> TimeAnchor.DURING_WORKOUT
            AnchorTypeUi.AFTER_WORKOUT -> TimeAnchor.AFTER_WORKOUT
            AnchorTypeUi.MIDNIGHT -> TimeAnchor.MIDNIGHT
            AnchorTypeUi.SNACK -> TimeAnchor.SNACK
        }
    }

    private fun timeAnchorToUi(value: TimeAnchor): AnchorTypeUi {
        return when (value) {
            TimeAnchor.WAKEUP -> AnchorTypeUi.WAKE_UP
            TimeAnchor.BREAKFAST -> AnchorTypeUi.BREAKFAST
            TimeAnchor.LUNCH -> AnchorTypeUi.LUNCH
            TimeAnchor.DINNER -> AnchorTypeUi.DINNER
            TimeAnchor.SLEEP -> AnchorTypeUi.SLEEP
            TimeAnchor.BEFORE_WORKOUT -> AnchorTypeUi.BEFORE_WORKOUT
            TimeAnchor.DURING_WORKOUT -> AnchorTypeUi.DURING_WORKOUT
            TimeAnchor.AFTER_WORKOUT -> AnchorTypeUi.AFTER_WORKOUT
            TimeAnchor.SNACK -> AnchorTypeUi.SNACK
            TimeAnchor.MIDNIGHT -> AnchorTypeUi.MIDNIGHT
        }
    }

    private fun formatLocalTime(time: KtxLocalTime): String {
        val hour = time.hour.toString().padStart(2, '0')
        val minute = time.minute.toString().padStart(2, '0')
        return "$hour:$minute"
    }

    private fun formatTimestamp(timestamp: Long): String {
        return Instant.ofEpochMilli(timestamp)
            .atZone(zoneId)
            .format(timeFormatter)
    }

    private fun buildIntensityLabel(
        activity: ActivityEntity,
        isScheduled: Boolean
    ): String? {
        val pieces = buildList {
            activity.intensity?.let { add("Intensity: $it") }
            if (activity.isWorkout) add("Workout")
            if (isScheduled) add("Scheduled")
        }

        return pieces.takeIf { it.isNotEmpty() }?.joinToString(" • ")
    }

    private fun buildScheduleSaveErrors(
        parsedSchedules: List<ParseResult>
    ): List<String> {
        return parsedSchedules.mapIndexedNotNull { index, result ->
            val invalid = result as? ParseResult.Invalid ?: return@mapIndexedNotNull null
            val message = invalid.errors
                .distinct()
                .joinToString("; ") { it.toDisplayText() }

            "Schedule ${index + 1}: $message"
        }
    }

    private fun ScheduleValidationError.toDisplayText(): String {
        return when (this) {
            ScheduleValidationError.MissingStartDate -> "Start date is required."
            ScheduleValidationError.InvalidInterval -> "Interval must be a number greater than 0."
            ScheduleValidationError.NoWeekdaysSelected -> "Select at least one weekday."
            ScheduleValidationError.NoTimesDefined -> "Add at least one time."
            is ScheduleValidationError.InvalidTimeFormat -> "One or more fixed times are invalid."
            is ScheduleValidationError.InvalidOffset -> "One or more anchor offsets are invalid."
            ScheduleValidationError.EndDateBeforeStartDate -> "End date cannot be before start date."
        }
    }
}

private fun ActivityType.toDisplayLabel(): String =
    name
        .lowercase()
        .split("_")
        .joinToString(" ") { word -> word.replaceFirstChar { it.uppercase() } }