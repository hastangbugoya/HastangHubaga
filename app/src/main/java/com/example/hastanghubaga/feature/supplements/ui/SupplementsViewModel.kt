package com.example.hastanghubaga.feature.supplements.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hastanghubaga.data.local.dao.supplement.SupplementEntityDao
import com.example.hastanghubaga.data.local.dao.supplement.SupplementScheduleDao
import com.example.hastanghubaga.data.local.dao.supplement.SupplementScheduleWriteModel
import com.example.hastanghubaga.data.local.entity.supplement.DoseAnchorType
import com.example.hastanghubaga.data.local.entity.supplement.FrequencyType
import com.example.hastanghubaga.data.local.entity.supplement.ScheduleRecurrenceType
import com.example.hastanghubaga.data.local.entity.supplement.ScheduleTimingType
import com.example.hastanghubaga.data.local.entity.supplement.SupplementDoseUnit
import com.example.hastanghubaga.data.local.entity.supplement.SupplementEntity
import com.example.hastanghubaga.data.local.entity.supplement.SupplementScheduleAnchoredTimeEntity
import com.example.hastanghubaga.data.local.entity.supplement.SupplementScheduleEntity
import com.example.hastanghubaga.data.local.entity.supplement.SupplementScheduleFixedTimeEntity
import com.example.hastanghubaga.domain.schedule.model.TimeAnchor
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

data class SupplementsUiState(
    val items: List<SupplementListItemUi> = emptyList(),
    val editor: SupplementEditorUiState? = null
)

data class SupplementEditorUiState(
    val id: Long? = null,
    val name: String = "",
    val brand: String = "",
    val notes: String = "",
    val isActive: Boolean = true,
    val isNew: Boolean = true,
    val scheduleEditors: List<ScheduleEditorState> = listOf(
        ScheduleEditorReducer.initialState()
    ),
    val scheduleSaveErrors: List<String> = emptyList()
)

@HiltViewModel
class SupplementsViewModel @Inject constructor(
    private val supplementEntityDao: SupplementEntityDao,
    private val supplementScheduleDao: SupplementScheduleDao
) : ViewModel() {

    private val editorState = MutableStateFlow<SupplementEditorUiState?>(null)

    private val itemsFlow =
        supplementEntityDao
            .getAllSupplementsFlow()
            .map { supplements ->
                supplements.map { supplement ->
                    SupplementListItemUi(
                        id = supplement.id,
                        name = supplement.name,
                        brand = supplement.brand,
                        notes = supplement.notes,
                        isActive = supplement.isActive
                    )
                }
            }

    val state: StateFlow<SupplementsUiState> =
        combine(
            itemsFlow,
            editorState
        ) { items, editor ->
            SupplementsUiState(
                items = items,
                editor = editor
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SupplementsUiState()
        )

    fun onAddClick() {
        editorState.value = SupplementEditorUiState(
            isNew = true,
            scheduleEditors = listOf(ScheduleEditorReducer.initialState())
        )
    }

    fun onEditClick(id: Long) {
        viewModelScope.launch {
            val supplement = supplementEntityDao.getSupplementById(id) ?: return@launch
            val persistedSchedules = supplementScheduleDao.getSchedulesForSupplement(id)

            val mappedScheduleEditors = if (persistedSchedules.isEmpty()) {
                listOf(ScheduleEditorReducer.initialState())
            } else {
                persistedSchedules.map { schedule ->
                    val fixedTimes = supplementScheduleDao.getFixedTimesForSchedule(schedule.id)
                    val anchoredTimes = supplementScheduleDao.getAnchoredTimesForSchedule(schedule.id)
                    persistedScheduleToEditorState(
                        schedule = schedule,
                        fixedTimes = fixedTimes,
                        anchoredTimes = anchoredTimes
                    )
                }
            }

            editorState.value = SupplementEditorUiState(
                id = supplement.id,
                name = supplement.name,
                brand = supplement.brand.orEmpty(),
                notes = supplement.notes.orEmpty(),
                isActive = supplement.isActive,
                isNew = false,
                scheduleEditors = mappedScheduleEditors,
                scheduleSaveErrors = emptyList()
            )
        }
    }

    fun onNameChanged(value: String) {
        editorState.update { current -> current?.copy(name = value) }
    }

    fun onBrandChanged(value: String) {
        editorState.update { current -> current?.copy(brand = value) }
    }

    fun onNotesChanged(value: String) {
        editorState.update { current -> current?.copy(notes = value) }
    }

    fun onIsActiveChanged(value: Boolean) {
        editorState.update { current -> current?.copy(isActive = value) }
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
        val trimmedName = editor.name.trim()
        if (trimmedName.isBlank()) return

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
            val supplementId = if (editor.isNew) {
                supplementEntityDao.insertSupplement(
                    SupplementEntity(
                        name = trimmedName,
                        brand = editor.brand.trim().ifBlank { null },
                        notes = editor.notes.trim().ifBlank { null },
                        recommendedServingSize = 1.0,
                        recommendedDoseUnit = SupplementDoseUnit.CAPSULE,
                        servingsPerDay = 1.0,
                        doseAnchorType = DoseAnchorType.MIDNIGHT,
                        frequencyType = FrequencyType.DAILY,
                        isActive = editor.isActive
                    )
                )
            } else {
                val existing = editor.id?.let { supplementEntityDao.getSupplementById(it) }
                    ?: return@launch

                supplementEntityDao.updateSupplement(
                    existing.copy(
                        name = trimmedName,
                        brand = editor.brand.trim().ifBlank { null },
                        notes = editor.notes.trim().ifBlank { null },
                        isActive = editor.isActive
                    )
                )
                existing.id
            }

            val writeModels = validParsedSchedules.map { draft ->
                parsedDraftToWriteModel(
                    supplementId = supplementId,
                    draft = draft
                )
            }

            supplementScheduleDao.replaceSchedulesForSupplement(
                supplementId = supplementId,
                schedules = writeModels
            )

            editorState.value = null
        }
    }

    fun onDeleteClick() {
        val editor = editorState.value ?: return
        val id = editor.id ?: return

        viewModelScope.launch {
            val existing = supplementEntityDao.getSupplementById(id) ?: return@launch
            supplementEntityDao.deleteSupplement(existing)
            editorState.value = null
        }
    }

    private fun persistedScheduleToEditorState(
        schedule: SupplementScheduleEntity,
        fixedTimes: List<SupplementScheduleFixedTimeEntity>,
        anchoredTimes: List<SupplementScheduleAnchoredTimeEntity>
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
        supplementId: Long,
        draft: ParsedScheduleEditorDraft
    ): SupplementScheduleWriteModel {
        val scheduleEntity = SupplementScheduleEntity(
            supplementId = supplementId,
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

        return SupplementScheduleWriteModel(
            schedule = scheduleEntity,
            fixedTimes = fixedTimes,
            anchoredTimes = anchoredTimes
        )
    }

    private fun parsedFixedTimeToEntity(
        row: ParsedFixedTimeRow,
        sortOrder: Int
    ): SupplementScheduleFixedTimeEntity {
        return SupplementScheduleFixedTimeEntity(
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
    ): SupplementScheduleAnchoredTimeEntity {
        return SupplementScheduleAnchoredTimeEntity(
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

    /**
     * Maps editor anchor choices to the shared scheduling domain anchor enum.
     *
     * Important:
     * - Use direct enum references rather than string-based valueOf lookups.
     * - The shared domain enum uses WAKEUP, not WAKE_UP.
     */
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
        }
    }

    /**
     * Maps persisted shared scheduling anchors back into the editor UI model.
     */
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
            else -> AnchorTypeUi.WAKE_UP
        }
    }

    private fun formatLocalTime(time: KtxLocalTime): String {
        val hour = time.hour.toString().padStart(2, '0')
        val minute = time.minute.toString().padStart(2, '0')
        return "$hour:$minute"
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