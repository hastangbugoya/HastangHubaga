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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.datetime.DayOfWeek as KtxDayOfWeek
import kotlinx.datetime.LocalTime as KtxLocalTime

data class ActivitiesUiState(
    val items: List<ActivityListItemUi> = emptyList(),
    val editor: ActivityEditorUiState? = null
)

data class ActivityEditorUiState(
    val id: Long? = null,
    val title: String = "",
    val type: ActivityType = ActivityType.OTHER,
    val notes: String = "",
    val intensity: String = "",
    val isWorkout: Boolean = false,
    val isActive: Boolean = true,
    val isNew: Boolean = true,
    val startHour: Int = 8,
    val startMinute: Int = 0,
    val durationHoursInput: String = "1",
    val durationMinutesInput: String = "0",
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
    private val scheduleTimeFormatter = DateTimeFormatter.ofPattern("h:mm a")

    private val itemsFlow =
        combine(
            activityEntityDao.observeAllActivities(),
            activityScheduleDao.observeAllSchedules(),
            activityScheduleDao.observeAllFixedTimes(),
            activityScheduleDao.observeAllAnchoredTimes()
        ) { activities, schedules, fixedTimes, anchoredTimes ->
            activities.map { activity ->
                val enabledSchedules = schedules
                    .filter { it.activityId == activity.id && it.isEnabled }

                val hasSchedule = enabledSchedules.isNotEmpty()

                ActivityListItemUi(
                    id = activity.id,
                    title = activity.title,
                    typeLabel = activity.type.toDisplayLabel(),
                    notes = activity.notes,
                    intensityLabel = buildIntensityLabel(
                        activity = activity,
                        hasSchedule = hasSchedule
                    ),
                    startLabel = buildStartLabel(
                        activity = activity,
                        enabledSchedules = enabledSchedules,
                        fixedTimes = fixedTimes,
                        anchoredTimes = anchoredTimes
                    ),
                    isActive = activity.isActive,
                    hasSchedule = hasSchedule
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
        val now = LocalDateTime.now(zoneId)

        editorState.value = ActivityEditorUiState(
            title = "",
            isNew = true,
            isWorkout = false,
            isActive = true,
            startHour = now.hour,
            startMinute = now.minute,
            durationHoursInput = "1",
            durationMinutesInput = "0",
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

            val startDateTime = Instant
                .ofEpochMilli(activity.startTimestamp)
                .atZone(zoneId)
                .toLocalDateTime()

            val derivedDurationMinutes = buildDurationMinutes(
                startTimestamp = activity.startTimestamp,
                endTimestamp = activity.endTimestamp
            )

            editorState.value = ActivityEditorUiState(
                id = activity.id,
                title = activity.title,
                type = activity.type,
                notes = activity.notes.orEmpty(),
                intensity = activity.intensity?.toString().orEmpty(),
                isWorkout = activity.isWorkout,
                isActive = activity.isActive,
                isNew = false,
                startHour = startDateTime.hour,
                startMinute = startDateTime.minute,
                durationHoursInput = (derivedDurationMinutes / 60).toString(),
                durationMinutesInput = (derivedDurationMinutes % 60).toString(),
                scheduleEditors = mappedScheduleEditors,
                scheduleSaveErrors = emptyList()
            )
        }
    }

    fun onTitleChanged(value: String) {
        editorState.update { current -> current?.copy(title = value) }
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

    fun onIsActiveChanged(value: Boolean) {
        editorState.update { current -> current?.copy(isActive = value) }
    }

    fun onStartTimeChanged(hour: Int, minute: Int) {
        editorState.update { current ->
            current?.copy(
                startHour = hour.coerceIn(0, 23),
                startMinute = minute.coerceIn(0, 59)
            )
        }
    }

    fun onDurationHoursChanged(value: String) {
        editorState.update { current ->
            current?.copy(durationHoursInput = value.filter { it.isDigit() })
        }
    }

    fun onDurationMinutesChanged(value: String) {
        editorState.update { current ->
            current?.copy(durationMinutesInput = value.filter { it.isDigit() })
        }
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

            val durationMinutes = parseDurationMinutes(
                hoursInput = editor.durationHoursInput,
                minutesInput = editor.durationMinutesInput
            )

            val startTimestamp = buildEditorStartTimestamp(
                hour = editor.startHour,
                minute = editor.startMinute
            )

            val endTimestamp = startTimestamp + (durationMinutes * 60_000L)

            val normalizedTitle = editor.title.trim()

            val activityId = if (editor.isNew) {
                activityEntityDao.insertActivity(
                    ActivityEntity(
                        title = normalizedTitle.ifBlank { editor.type.toDisplayLabel() },
                        type = editor.type,
                        startTimestamp = startTimestamp,
                        endTimestamp = endTimestamp,
                        notes = editor.notes.trim().ifBlank { null },
                        intensity = parsedIntensity,
                        isWorkout = editor.isWorkout,
                        isActive = editor.isActive
                    )
                )
            } else {
                val existing = editor.id?.let { activityEntityDao.getActivityById(it) }
                    ?: return@launch

                activityEntityDao.updateActivity(
                    existing.copy(
                        title = normalizedTitle.ifBlank { existing.title },
                        type = editor.type,
                        startTimestamp = startTimestamp,
                        endTimestamp = endTimestamp,
                        notes = editor.notes.trim().ifBlank { null },
                        intensity = parsedIntensity,
                        isWorkout = editor.isWorkout,
                        isActive = editor.isActive
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

    private fun buildStartLabel(
        activity: ActivityEntity,
        enabledSchedules: List<ActivityScheduleEntity>,
        fixedTimes: List<ActivityScheduleFixedTimeEntity>,
        anchoredTimes: List<ActivityScheduleAnchoredTimeEntity>
    ): String {
        val firstEnabledSchedule = enabledSchedules.minByOrNull { it.id }
            ?: return formatTimestamp(activity.startTimestamp)

        return when (firstEnabledSchedule.timingType) {
            ScheduleTimingType.FIXED -> {
                val firstFixed = fixedTimes
                    .filter { it.scheduleId == firstEnabledSchedule.id }
                    .minWithOrNull(compareBy<ActivityScheduleFixedTimeEntity> { it.sortOrder }.thenBy { it.id })

                firstFixed?.time?.let(::formatScheduleTime) ?: formatTimestamp(activity.startTimestamp)
            }

            ScheduleTimingType.ANCHORED -> {
                val firstAnchored = anchoredTimes
                    .filter { it.scheduleId == firstEnabledSchedule.id }
                    .minWithOrNull(compareBy<ActivityScheduleAnchoredTimeEntity> { it.sortOrder }.thenBy { it.id })

                firstAnchored?.let { anchored ->
                    buildAnchoredLabel(
                        anchor = anchored.anchor,
                        offsetMinutes = anchored.offsetMinutes
                    )
                } ?: "Anchored"
            }
        }
    }

    private fun buildAnchoredLabel(
        anchor: TimeAnchor,
        offsetMinutes: Int
    ): String {
        val anchorLabel = when (anchor) {
            TimeAnchor.MIDNIGHT -> "Midnight"
            TimeAnchor.WAKEUP -> "Wake up"
            TimeAnchor.BREAKFAST -> "Breakfast"
            TimeAnchor.LUNCH -> "Lunch"
            TimeAnchor.DINNER -> "Dinner"
            TimeAnchor.SNACK -> "Snack"
            TimeAnchor.BEFORE_WORKOUT -> "Before workout"
            TimeAnchor.DURING_WORKOUT -> "During workout"
            TimeAnchor.AFTER_WORKOUT -> "After workout"
            TimeAnchor.SLEEP -> "Sleep"
        }

        return when {
            offsetMinutes == 0 -> anchorLabel
            offsetMinutes > 0 -> "$anchorLabel +${offsetMinutes}m"
            else -> "$anchorLabel ${offsetMinutes}m"
        }
    }

    private fun formatScheduleTime(time: KtxLocalTime): String {
        return java.time.LocalTime.of(time.hour, time.minute, time.second)
            .format(scheduleTimeFormatter)
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
        hasSchedule: Boolean
    ): String? {
        val pieces = buildList {
            activity.intensity?.let { add("Intensity: $it") }
            if (activity.isWorkout) add("Workout")
            if (!activity.isActive) add("Inactive")
            if (hasSchedule) add("Scheduled")
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

    private fun buildEditorStartTimestamp(
        hour: Int,
        minute: Int
    ): Long {
        val today = DomainTimePolicy.todayLocal()
        val localDateTime = LocalDateTime.of(
            today.year,
            today.monthNumber,
            today.dayOfMonth,
            hour.coerceIn(0, 23),
            minute.coerceIn(0, 59)
        )

        return localDateTime
            .atZone(zoneId)
            .toInstant()
            .toEpochMilli()
    }

    private fun parseDurationMinutes(
        hoursInput: String,
        minutesInput: String
    ): Int {
        val hours = hoursInput.toIntOrNull()?.coerceAtLeast(0) ?: 0
        val minutes = minutesInput.toIntOrNull()?.coerceAtLeast(0) ?: 0
        return (hours * 60) + minutes
    }

    private fun buildDurationMinutes(
        startTimestamp: Long,
        endTimestamp: Long?
    ): Int {
        val safeEnd = endTimestamp ?: return 60
        val diffMs = safeEnd - startTimestamp
        if (diffMs <= 0L) return 60
        return (diffMs / 60_000L).toInt()
    }
}

private fun ActivityType.toDisplayLabel(): String =
    name
        .lowercase()
        .split("_")
        .joinToString(" ") { word -> word.replaceFirstChar { it.uppercase() } }