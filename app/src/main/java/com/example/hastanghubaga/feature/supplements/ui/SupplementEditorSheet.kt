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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.hastanghubaga.feature.schedule.ui.ScheduleEditorSection
import com.example.hastanghubaga.feature.schedule.ui.model.ScheduleEditorController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupplementEditorSheet(
    state: SupplementEditorUiState,
    onNameChanged: (String) -> Unit,
    onBrandChanged: (String) -> Unit,
    onNotesChanged: (String) -> Unit,
    onIsActiveChanged: (Boolean) -> Unit,
    onSaveClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isExisting = !state.isNew

    // -------------------------
    // Schedule editor (UI-only for now)
    // -------------------------
    val scheduleController = remember {
        ScheduleEditorController()
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
            text = "First-pass editor: basic fields only. Scheduling and dose settings can come later.",
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

        // -------------------------
        // NEW: Schedule section
        // -------------------------
        Spacer(Modifier.height(8.dp))
        HorizontalDivider()
        Spacer(Modifier.height(4.dp))

        Text(
            text = "Schedule",
            style = MaterialTheme.typography.titleMedium
        )

        ScheduleEditorSection(
            state = scheduleController.state,
            onEnabledChanged = { scheduleController.setEnabled(it) },
            onRecurrenceModeChanged = { scheduleController.setRecurrenceMode(it) },
            onIntervalInputChanged = { scheduleController.setIntervalInput(it) },
            onWeekdayToggled = { scheduleController.toggleWeekday(it) },
            onStartDateClick = {
                // TODO: hook up date picker
            },
            onEndDateToggleChanged = { scheduleController.setHasEndDate(it) },
            onEndDateClick = {
                // TODO: hook up date picker
            },
            onTimingModeChanged = { scheduleController.setTimingMode(it) },
            onFixedTimeChanged = { id, value ->
                scheduleController.setFixedTimeValue(id, value)
            },
            onAddFixedTime = { scheduleController.addFixedTimeRow() },
            onRemoveFixedTime = { scheduleController.removeFixedTimeRow(it) },
            onAnchoredRowAnchorChanged = { id, anchor ->
                scheduleController.setAnchoredRowAnchor(id, anchor)
            },
            onAnchoredRowOffsetChanged = { id, value ->
                scheduleController.setAnchoredRowOffsetValue(id, value)
            },
            onAddAnchoredRow = { scheduleController.addAnchoredTimeRow() },
            onRemoveAnchoredRow = { scheduleController.removeAnchoredTimeRow(it) }
        )

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