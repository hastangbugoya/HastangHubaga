package com.example.hastanghubaga.feature.meals.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
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
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.hastanghubaga.data.local.entity.meal.MealType
import com.example.hastanghubaga.feature.schedule.ui.ScheduleEditorSection
import com.example.hastanghubaga.feature.schedule.ui.model.ScheduleEditorState
import com.example.hastanghubaga.feature.schedule.ui.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealEditorSheet(
    state: MealEditorUiState,

    // meal fields
    onNameChanged: (String) -> Unit,
    onTypeChanged: (MealType) -> Unit,
    onTreatAsAnchorChanged: (MealType?) -> Unit,
    onIsActiveChanged: (Boolean) -> Unit,
    onNotesChanged: (String) -> Unit,

    // schedule state + handlers
    scheduleState: ScheduleEditorState,
    onEnabledChanged: (Boolean) -> Unit,
    onRecurrenceModeChanged: (RecurrenceMode) -> Unit,
    onIntervalInputChanged: (String) -> Unit,
    onWeekdayToggled: (WeekdayUi) -> Unit,
    onStartDateClick: () -> Unit,
    onEndDateToggleChanged: (Boolean) -> Unit,
    onEndDateClick: () -> Unit,
    onTimingModeChanged: (TimingMode) -> Unit,
    onFixedTimeChanged: (rowId: String, value: String) -> Unit,
    onAddFixedTime: () -> Unit,
    onRemoveFixedTime: (rowId: String) -> Unit,
    onAnchoredRowAnchorChanged: (rowId: String, anchor: AnchorTypeUi) -> Unit,
    onAnchoredRowOffsetChanged: (rowId: String, value: String) -> Unit,
    onAddAnchoredRow: () -> Unit,
    onRemoveAnchoredRow: (rowId: String) -> Unit,

    // actions
    onSaveClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onDismiss: () -> Unit,

    modifier: Modifier = Modifier
) {
    val isExisting = !state.isNew
    var typeMenuExpanded by remember { mutableStateOf(false) }
    var treatAsMenuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = if (state.isNew) "Add meal" else "Edit meal",
            style = MaterialTheme.typography.titleLarge
        )

        Text(
            text = "Meals are reusable templates. Scheduling below determines when they appear in the timeline.",
            style = MaterialTheme.typography.bodyMedium
        )

        OutlinedTextField(
            value = state.name,
            onValueChange = onNameChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Meal name") },
            singleLine = true
        )

        // TYPE
        ExposedDropdownMenuBox(
            expanded = typeMenuExpanded,
            onExpandedChange = { typeMenuExpanded = !typeMenuExpanded }
        ) {
            OutlinedTextField(
                value = state.type.toDisplayLabel(),
                onValueChange = {},
                readOnly = true,
                label = { Text("Meal type") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(typeMenuExpanded)
                },
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = typeMenuExpanded,
                onDismissRequest = { typeMenuExpanded = false }
            ) {
                MealType.values().forEach {
                    DropdownMenuItem(
                        text = { Text(it.toDisplayLabel()) },
                        onClick = {
                            onTypeChanged(it)
                            typeMenuExpanded = false
                        }
                    )
                }
            }
        }

        // TREAT AS
        ExposedDropdownMenuBox(
            expanded = treatAsMenuExpanded,
            onExpandedChange = { treatAsMenuExpanded = !treatAsMenuExpanded }
        ) {
            OutlinedTextField(
                value = state.treatAsAnchor?.toDisplayLabel() ?: "None",
                onValueChange = {},
                readOnly = true,
                label = { Text("Treat as anchor") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(treatAsMenuExpanded)
                },
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = treatAsMenuExpanded,
                onDismissRequest = { treatAsMenuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("None") },
                    onClick = {
                        onTreatAsAnchorChanged(null)
                        treatAsMenuExpanded = false
                    }
                )

                anchorEligibleMealTypes().forEach {
                    DropdownMenuItem(
                        text = { Text(it.toDisplayLabel()) },
                        onClick = {
                            onTreatAsAnchorChanged(it)
                            treatAsMenuExpanded = false
                        }
                    )
                }
            }
        }

        // ACTIVE
        RowWithSwitch(
            label = "Active",
            checked = state.isActive,
            onCheckedChange = onIsActiveChanged
        )

        OutlinedTextField(
            value = state.notes,
            onValueChange = onNotesChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Notes") },
            minLines = 3
        )

        Spacer(Modifier.height(4.dp))
        HorizontalDivider()

        // 🔥 REAL SCHEDULER (THIS IS THE KEY)
        ScheduleEditorSection(
            state = scheduleState,
            onEnabledChanged = onEnabledChanged,
            onRecurrenceModeChanged = onRecurrenceModeChanged,
            onIntervalInputChanged = onIntervalInputChanged,
            onWeekdayToggled = onWeekdayToggled,
            onStartDateClick = onStartDateClick,
            onEndDateToggleChanged = onEndDateToggleChanged,
            onEndDateClick = onEndDateClick,
            onTimingModeChanged = onTimingModeChanged,
            onFixedTimeChanged = onFixedTimeChanged,
            onAddFixedTime = onAddFixedTime,
            onRemoveFixedTime = onRemoveFixedTime,
            onAnchoredRowAnchorChanged = onAnchoredRowAnchorChanged,
            onAnchoredRowOffsetChanged = onAnchoredRowOffsetChanged,
            onAddAnchoredRow = onAddAnchoredRow,
            onRemoveAnchoredRow = onRemoveAnchoredRow
        )

        Spacer(Modifier.height(8.dp))
        HorizontalDivider()

        Button(
            onClick = onSaveClick,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 14.dp)
        ) {
            Text(if (state.isNew) "Create" else "Save")
        }

        TextButton(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cancel")
        }

        if (isExisting) {
            TextButton(
                onClick = onDeleteClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Delete meal")
            }
        }
    }
}

@Composable
private fun RowWithSwitch(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

private fun MealType.toDisplayLabel(): String =
    name.lowercase().split("_").joinToString(" ") {
        it.replaceFirstChar(Char::uppercase)
    }

private fun anchorEligibleMealTypes(): List<MealType> =
    listOf(MealType.BREAKFAST, MealType.LUNCH, MealType.DINNER)