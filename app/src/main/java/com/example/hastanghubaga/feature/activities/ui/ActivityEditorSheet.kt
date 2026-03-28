package com.example.hastanghubaga.feature.activities.ui

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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.hastanghubaga.domain.model.activity.ActivityType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityEditorSheet(
    state: ActivityEditorUiState,
    onTypeChanged: (ActivityType) -> Unit,
    onNotesChanged: (String) -> Unit,
    onIntensityChanged: (String) -> Unit,
    onSaveClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isExisting = !state.isNew
    var typeMenuExpanded by remember { mutableStateOf(false) }

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
            text = "First-pass editor: basic fields only. Activity settings can come later.",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(Modifier.height(4.dp))

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