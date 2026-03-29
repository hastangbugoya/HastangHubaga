package com.example.hastanghubaga.feature.supplements.ui

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
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.unit.dp
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupplementEditorSheet(
    state: SupplementEditorUiState,
    onNameChanged: (String) -> Unit,
    onBrandChanged: (String) -> Unit,
    onNotesChanged: (String) -> Unit,
    onIsActiveChanged: (Boolean) -> Unit,
    onAddScheduleClick: () -> Unit,
    onRemoveScheduleClick: (Int) -> Unit,
    onScheduleAction: (Int, ScheduleEditorAction) -> Unit,
    onSaveClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isExisting = !state.isNew

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
            text = if (state.isNew) "Add supplement" else "Edit supplement",
            style = MaterialTheme.typography.titleLarge
        )

        Text(
            text = "Basic supplement fields plus actual schedule rules. Recommendation fields can remain separate from actual planning.",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(Modifier.height(4.dp))

        OutlinedTextField(
            value = state.name,
            onValueChange = onNameChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Name") },
            singleLine = true
        )

        OutlinedTextField(
            value = state.brand,
            onValueChange = onBrandChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Brand") },
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
                    text = "Inactive supplements stay stored but can be hidden from active usage.",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Switch(
                checked = state.isActive,
                onCheckedChange = onIsActiveChanged
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
                contentPadding = PaddingValues(vertical = 14.dp),
                enabled = state.name.trim().isNotEmpty()
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
                Text("Delete supplement")
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

private fun LocalDate.toEpochMillisUtc(): Long {
    return atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
}

private fun Long.toKtxLocalDateUtc(): LocalDate {
    return Instant
        .fromEpochMilliseconds(this)
        .toLocalDateTime(TimeZone.UTC)
        .date
}