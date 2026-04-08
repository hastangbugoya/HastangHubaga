package com.example.hastanghubaga.feature.activities.ui

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.hastanghubaga.domain.model.activity.ActivityType
import com.example.hastanghubaga.feature.schedule.ui.ScheduleEditorSection
import com.example.hastanghubaga.feature.schedule.ui.model.ScheduleEditorAction
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime

private enum class ScheduleDatePickerTarget {
    START,
    END
}

@Composable
private fun StartTimeDurationSection(
    startHour: Int,
    startMinute: Int,
    durationHoursInput: String,
    durationMinutesInput: String,
    onStartTimeChanged: (hour: Int, minute: Int) -> Unit,
    onDurationHoursChanged: (String) -> Unit,
    onDurationMinutesChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Time",
            style = MaterialTheme.typography.titleMedium
        )

        OutlinedTextField(
            value = formatTime(startHour, startMinute),
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Start time") }
        )

        TextButton(
            onClick = {
                TimePickerDialog(
                    context,
                    { _, hourOfDay, minute ->
                        onStartTimeChanged(hourOfDay, minute)
                    },
                    startHour,
                    startMinute,
                    false
                ).show()
            }
        ) {
            Text("Change start time")
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = durationHoursInput,
                onValueChange = onDurationHoursChanged,
                modifier = Modifier.weight(1f),
                label = { Text("Duration hours") },
                singleLine = true
            )

            OutlinedTextField(
                value = durationMinutesInput,
                onValueChange = onDurationMinutesChanged,
                modifier = Modifier.weight(1f),
                label = { Text("Duration minutes") },
                singleLine = true
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityEditorSheet(
    state: ActivityEditorUiState,
    onTitleChanged: (String) -> Unit,
    onTypeChanged: (ActivityType) -> Unit,
    onNotesChanged: (String) -> Unit,
    onIntensityChanged: (String) -> Unit,
    onIsWorkoutChanged: (Boolean) -> Unit,
    onIsActiveChanged: (Boolean) -> Unit,
    onStartTimeChanged: (hour: Int, minute: Int) -> Unit,
    onDurationHoursChanged: (String) -> Unit,
    onDurationMinutesChanged: (String) -> Unit,
    onAddScheduleClick: () -> Unit,
    onRemoveScheduleClick: (Int) -> Unit,
    onScheduleAction: (Int, ScheduleEditorAction) -> Unit,
    onSaveClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isExisting = !state.isNew
    var typeMenuExpanded by remember { mutableStateOf(false) }

    var pickerScheduleIndex by remember { mutableIntStateOf(-1) }
    var pickerTarget by remember { mutableStateOf<ScheduleDatePickerTarget?>(null) }

    val activePickerDate: LocalDate? =
        state.scheduleEditors
            .getOrNull(pickerScheduleIndex)
            ?.let { schedule ->
                when (pickerTarget) {
                    ScheduleDatePickerTarget.START -> schedule.startDate
                    ScheduleDatePickerTarget.END -> schedule.endDate
                    null -> null
                }
            }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = activePickerDate?.toEpochMillisUtc()
    )

    if (pickerTarget != null && pickerScheduleIndex >= 0) {
        DatePickerDialog(
            onDismissRequest = {
                pickerTarget = null
                pickerScheduleIndex = -1
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedMillis = datePickerState.selectedDateMillis
                        if (selectedMillis != null) {
                            val selectedDate = selectedMillis.toKtxLocalDateUtc()
                            when (pickerTarget) {
                                ScheduleDatePickerTarget.START -> {
                                    onScheduleAction(
                                        pickerScheduleIndex,
                                        ScheduleEditorAction.SetStartDate(selectedDate)
                                    )
                                }

                                ScheduleDatePickerTarget.END -> {
                                    onScheduleAction(
                                        pickerScheduleIndex,
                                        ScheduleEditorAction.SetEndDate(selectedDate)
                                    )
                                }

                                null -> Unit
                            }
                        }

                        pickerTarget = null
                        pickerScheduleIndex = -1
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        pickerTarget = null
                        pickerScheduleIndex = -1
                    }
                ) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(
                state = datePickerState
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = if (state.isNew) "Add activity" else "Edit activity",
            style = MaterialTheme.typography.titleLarge
        )

        Text(
            text = "Basic activity fields plus actual schedule rules. Planned activity occurrences remain the authoritative source for planned timeline presence.",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(Modifier.height(4.dp))

        OutlinedTextField(
            value = state.title,
            onValueChange = onTitleChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Activity title") },
            singleLine = true
        )

        ExposedDropdownMenuBox(
            expanded = typeMenuExpanded,
            onExpandedChange = { typeMenuExpanded = !typeMenuExpanded }
        ) {
            OutlinedTextField(
                value = state.type.toDisplayLabel(),
                onValueChange = {},
                readOnly = true,
                label = { Text("Activity type") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeMenuExpanded)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = typeMenuExpanded,
                onDismissRequest = { typeMenuExpanded = false }
            ) {
                ActivityType.entries.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type.toDisplayLabel()) },
                        onClick = {
                            onTypeChanged(type)
                            typeMenuExpanded = false
                        }
                    )
                }
            }
        }

        StartTimeDurationSection(
            startHour = state.startHour,
            startMinute = state.startMinute,
            durationHoursInput = state.durationHoursInput,
            durationMinutesInput = state.durationMinutesInput,
            onStartTimeChanged = onStartTimeChanged,
            onDurationHoursChanged = onDurationHoursChanged,
            onDurationMinutesChanged = onDurationMinutesChanged
        )

        OutlinedTextField(
            value = state.intensity,
            onValueChange = onIntensityChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Intensity (optional)") },
            singleLine = true
        )

        OutlinedTextField(
            value = state.notes,
            onValueChange = onNotesChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Notes") },
            minLines = 3
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Active",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Inactive activities stay stored but are excluded from planning and occurrence generation.",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Switch(
                checked = state.isActive,
                onCheckedChange = onIsActiveChanged
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Treat as workout",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Use this when before-, during-, or after-workout anchors should resolve through this activity.",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Switch(
                checked = state.isWorkout,
                onCheckedChange = onIsWorkoutChanged
            )
        }

        Spacer(Modifier.height(8.dp))
        HorizontalDivider()
        Spacer(Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Schedules",
                style = MaterialTheme.typography.titleMedium
            )

            TextButton(
                onClick = onAddScheduleClick
            ) {
                Text("Add schedule")
            }
        }

        if (state.scheduleSaveErrors.isNotEmpty()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                state.scheduleSaveErrors.forEach { error ->
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        state.scheduleEditors.forEachIndexed { index, scheduleState ->
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Schedule ${index + 1}",
                        style = MaterialTheme.typography.titleSmall
                    )

                    if (state.scheduleEditors.size > 1) {
                        TextButton(
                            onClick = { onRemoveScheduleClick(index) }
                        ) {
                            Text("Remove")
                        }
                    }
                }

                ScheduleEditorSection(
                    state = scheduleState,
                    onEnabledChanged = {
                        onScheduleAction(index, ScheduleEditorAction.SetEnabled(it))
                    },
                    onRecurrenceModeChanged = {
                        onScheduleAction(index, ScheduleEditorAction.SetRecurrenceMode(it))
                    },
                    onIntervalInputChanged = {
                        onScheduleAction(index, ScheduleEditorAction.SetIntervalInput(it))
                    },
                    onWeekdayToggled = {
                        onScheduleAction(index, ScheduleEditorAction.ToggleWeekday(it))
                    },
                    onStartDateClick = {
                        pickerScheduleIndex = index
                        pickerTarget = ScheduleDatePickerTarget.START
                    },
                    onEndDateToggleChanged = {
                        onScheduleAction(index, ScheduleEditorAction.SetHasEndDate(it))
                    },
                    onEndDateClick = {
                        pickerScheduleIndex = index
                        pickerTarget = ScheduleDatePickerTarget.END
                    },
                    onTimingModeChanged = {
                        onScheduleAction(index, ScheduleEditorAction.SetTimingMode(it))
                    },
                    onFixedTimeChanged = { rowId, value ->
                        onScheduleAction(
                            index,
                            ScheduleEditorAction.SetFixedTimeValue(
                                rowId = rowId,
                                value = value
                            )
                        )
                    },
                    onAddFixedTime = {
                        onScheduleAction(index, ScheduleEditorAction.AddFixedTimeRow)
                    },
                    onRemoveFixedTime = { rowId ->
                        onScheduleAction(
                            index,
                            ScheduleEditorAction.RemoveFixedTimeRow(rowId)
                        )
                    },
                    onAnchoredRowAnchorChanged = { rowId, anchor ->
                        onScheduleAction(
                            index,
                            ScheduleEditorAction.SetAnchoredRowAnchor(
                                rowId = rowId,
                                anchor = anchor
                            )
                        )
                    },
                    onAnchoredRowOffsetChanged = { rowId, value ->
                        onScheduleAction(
                            index,
                            ScheduleEditorAction.SetAnchoredRowOffsetValue(
                                rowId = rowId,
                                value = value
                            )
                        )
                    },
                    onAddAnchoredRow = {
                        onScheduleAction(index, ScheduleEditorAction.AddAnchoredTimeRow)
                    },
                    onRemoveAnchoredRow = { rowId ->
                        onScheduleAction(
                            index,
                            ScheduleEditorAction.RemoveAnchoredTimeRow(rowId)
                        )
                    }
                )

                if (index < state.scheduleEditors.lastIndex) {
                    Spacer(Modifier.height(4.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(4.dp))
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        HorizontalDivider()
        Spacer(Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Text("Cancel")
            }

            Button(
                onClick = onSaveClick,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Text(if (state.isNew) "Create" else "Save")
            }
        }

        if (isExisting) {
            TextButton(
                onClick = onDeleteClick,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Text("Delete activity")
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

private fun ActivityType.toDisplayLabel(): String =
    name
        .lowercase()
        .split("_")
        .joinToString(" ") { word -> word.replaceFirstChar { it.uppercase() } }

private fun LocalDate.toEpochMillisUtc(): Long {
    return atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
}

private fun Long.toKtxLocalDateUtc(): LocalDate {
    return Instant
        .fromEpochMilliseconds(this)
        .toLocalDateTime(TimeZone.UTC)
        .date
}

private fun formatTime(hour: Int, minute: Int): String {
    val displayHour = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    val displayMinute = minute.toString().padStart(2, '0')
    val amPm = if (hour < 12) "AM" else "PM"
    return "$displayHour:$displayMinute $amPm"
}