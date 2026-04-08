package com.example.hastanghubaga.feature.nutritiongoals.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.hastanghubaga.domain.model.nutrition.NutritionGoalType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NutritionGoalsEditorSheet(
    state: NutritionPlanEditorState,
    nutrientCatalog: List<NutritionGoalCatalogItemUi>,
    akImportedGoalPicker: AkImportedGoalPickerState,
    onNameChanged: (String) -> Unit,
    onTypeChanged: (NutritionGoalType) -> Unit,
    onStartDateChanged: (String) -> Unit,
    onEndDateChanged: (String) -> Unit,
    onIsActiveChanged: (Boolean) -> Unit,
    onReferToAkClick: () -> Unit,
    onDismissAkGoalPicker: () -> Unit,
    onAkGoalCheckedChanged: (String, Boolean) -> Unit,
    onApplySelectedAkGoals: () -> Unit,
    onAddGoalRow: () -> Unit,
    onRemoveGoalRow: (String) -> Unit,
    onGoalNutrientChanged: (String, String) -> Unit,
    onGoalMinChanged: (String, String) -> Unit,
    onGoalTargetChanged: (String, String) -> Unit,
    onGoalMaxChanged: (String, String) -> Unit,
    onSaveClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = if (state.isEditing) "Edit Nutrition Plan" else "Add Nutrition Plan",
            style = MaterialTheme.typography.headlineSmall
        )

        if (!state.errorMessage.isNullOrBlank()) {
            Text(
                text = state.errorMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }

        OutlinedTextField(
            value = state.name,
            onValueChange = onNameChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Plan name") },
            singleLine = true,
            enabled = !state.isSaving
        )

        NutritionGoalTypeDropdownField(
            selectedType = state.type,
            enabled = !state.isSaving,
            onTypeSelected = onTypeChanged
        )

        OutlinedTextField(
            value = state.startDateInput,
            onValueChange = onStartDateChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Start date (epoch millis)") },
            singleLine = true,
            enabled = !state.isSaving
        )

        OutlinedTextField(
            value = state.endDateInput,
            onValueChange = onEndDateChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("End date (epoch millis, optional)") },
            singleLine = true,
            enabled = !state.isSaving
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Plan active",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "Enable or disable this plan.",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Switch(
                checked = state.isActive,
                onCheckedChange = onIsActiveChanged,
                enabled = !state.isSaving
            )
        }

        HorizontalDivider()

        Text(
            text = "Nutrient goals",
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = "Each row can define a min, target, max, or any combination for one nutrient.",
            style = MaterialTheme.typography.bodySmall
        )

        OutlinedButton(
            onClick = onReferToAkClick,
            enabled = !state.isSaving,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 14.dp)
        ) {
            Text("Refer to AK")
        }

        if (akImportedGoalPicker.isVisible) {
            HorizontalDivider()

            Text(
                text = "AK reference",
                style = MaterialTheme.typography.titleSmall
            )

            akImportedGoalPicker.source?.takeIf { it.isNotBlank() }?.let { source ->
                Text(
                    text = "Source: $source",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            akImportedGoalPicker.exportedAtEpochMs?.let { exportedAt ->
                Text(
                    text = "AK exported at: $exportedAt",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            akImportedGoalPicker.loadedAtEpochMs?.let { loadedAt ->
                Text(
                    text = "Loaded into HH at: $loadedAt",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (!akImportedGoalPicker.errorMessage.isNullOrBlank()) {
                Text(
                    text = akImportedGoalPicker.errorMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            if (akImportedGoalPicker.isLoading) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.widthIn(max = 20.dp)
                    )
                    Text(
                        text = "Loading current AK goals...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else if (akImportedGoalPicker.items.isEmpty()) {
                Text(
                    text = "No AK goals available to reference right now.",
                    style = MaterialTheme.typography.bodySmall
                )
            } else {
                Text(
                    text = "Select AK goal rows to copy into this editor. Nothing is saved until you tap Save.",
                    style = MaterialTheme.typography.bodySmall
                )

                akImportedGoalPicker.items.forEach { item ->
                    AkImportedGoalPickerRow(
                        item = item,
                        checked = item.selectionKey in akImportedGoalPicker.selectedKeys,
                        enabled = !state.isSaving,
                        onCheckedChange = { isChecked ->
                            onAkGoalCheckedChanged(item.selectionKey, isChecked)
                        }
                    )

                    HorizontalDivider()
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismissAkGoalPicker,
                        enabled = !state.isSaving,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 14.dp)
                    ) {
                        Text("Close AK reference")
                    }

                    Button(
                        onClick = onApplySelectedAkGoals,
                        enabled = !state.isSaving,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 14.dp)
                    ) {
                        Text("Apply selected")
                    }
                }
            }

            HorizontalDivider()
        }

        state.goalRows.forEach { row ->
            GoalRowEditor(
                row = row,
                nutrientCatalog = nutrientCatalog,
                enabled = !state.isSaving,
                showRemoveButton = state.goalRows.size > 1,
                onRemoveClick = { onRemoveGoalRow(row.rowId) },
                onNutrientChanged = { onGoalNutrientChanged(row.rowId, it) },
                onMinChanged = { onGoalMinChanged(row.rowId, it) },
                onTargetChanged = { onGoalTargetChanged(row.rowId, it) },
                onMaxChanged = { onGoalMaxChanged(row.rowId, it) }
            )

            HorizontalDivider()
        }

        OutlinedButton(
            onClick = onAddGoalRow,
            enabled = !state.isSaving,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 14.dp)
        ) {
            Text("Add goal row")
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (state.isEditing) {
                OutlinedButton(
                    onClick = onDeleteClick,
                    enabled = !state.isSaving,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 14.dp)
                ) {
                    Text("Delete")
                }
            }

            Button(
                onClick = onSaveClick,
                enabled = !state.isSaving,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.widthIn(max = 20.dp)
                    )
                } else {
                    Text("Save")
                }
            }
        }

        TextButton(
            onClick = onDismiss,
            enabled = !state.isSaving,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cancel")
        }
    }
}

@Composable
private fun AkImportedGoalPickerRow(
    item: AkImportedGoalPickerItemUi,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = item.displayName,
                style = MaterialTheme.typography.titleSmall
            )

            Text(
                text = buildImportedGoalSummary(item),
                style = MaterialTheme.typography.bodySmall
            )

            Text(
                text = item.hhSupportText,
                style = MaterialTheme.typography.bodySmall
            )

            Text(
                text = buildImportedGoalMeta(item),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

private fun buildImportedGoalSummary(
    item: AkImportedGoalPickerItemUi
): String {
    val parts = buildList {
        item.minValue?.let { add("AK min $it") }
        item.targetValue?.let { add("AK target $it") }
        item.maxValue?.let { add("AK max $it") }
    }

    return parts.joinToString(" • ").ifBlank { "AK: no goal values" }
}

private fun buildImportedGoalMeta(
    item: AkImportedGoalPickerItemUi
): String {
    val unitPart = item.unitLabel?.takeIf { it.isNotBlank() } ?: "unit unknown"
    return "${item.sourceKindLabel} • ${item.nutrientKey} • $unitPart"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NutritionGoalTypeDropdownField(
    selectedType: NutritionGoalType,
    enabled: Boolean,
    onTypeSelected: (NutritionGoalType) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            if (enabled) {
                expanded = !expanded
            }
        }
    ) {
        OutlinedTextField(
            value = selectedType.displayName,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text("Plan type") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            NutritionGoalType.entries.forEach { type ->
                DropdownMenuItem(
                    text = { Text(type.displayName) },
                    onClick = {
                        expanded = false
                        onTypeSelected(type)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GoalRowEditor(
    row: NutritionGoalEditorRowUi,
    nutrientCatalog: List<NutritionGoalCatalogItemUi>,
    enabled: Boolean,
    showRemoveButton: Boolean,
    onRemoveClick: () -> Unit,
    onNutrientChanged: (String) -> Unit,
    onMinChanged: (String) -> Unit,
    onTargetChanged: (String) -> Unit,
    onMaxChanged: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        NutrientCatalogDropdownField(
            selectedNutrientKey = row.nutrientKey,
            selectedDisplayName = row.nutrientDisplayName,
            nutrientCatalog = nutrientCatalog,
            enabled = enabled,
            onNutrientSelected = onNutrientChanged
        )

        row.unitLabel?.takeIf { it.isNotBlank() }?.let { unitLabel ->
            Text(
                text = "Unit: $unitLabel",
                style = MaterialTheme.typography.bodySmall
            )
        }

        OutlinedTextField(
            value = row.minValueInput,
            onValueChange = onMinChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Min") },
            singleLine = true,
            enabled = enabled
        )

        OutlinedTextField(
            value = row.targetValueInput,
            onValueChange = onTargetChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Target") },
            singleLine = true,
            enabled = enabled
        )

        OutlinedTextField(
            value = row.maxValueInput,
            onValueChange = onMaxChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Max") },
            singleLine = true,
            enabled = enabled
        )

        if (showRemoveButton) {
            OutlinedButton(
                onClick = onRemoveClick,
                enabled = enabled,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                Text("Remove row")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NutrientCatalogDropdownField(
    selectedNutrientKey: String,
    selectedDisplayName: String,
    nutrientCatalog: List<NutritionGoalCatalogItemUi>,
    enabled: Boolean,
    onNutrientSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val selectedLabel = when {
        selectedDisplayName.isNotBlank() -> selectedDisplayName
        selectedNutrientKey.isNotBlank() -> selectedNutrientKey
        else -> ""
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            if (enabled) {
                expanded = !expanded
            }
        }
    ) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text("Nutrient") },
            placeholder = { Text("Select nutrient") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            nutrientCatalog.forEach { nutrient ->
                DropdownMenuItem(
                    text = {
                        val unitSuffix = nutrient.unitLabel
                            ?.takeIf { it.isNotBlank() }
                            ?.let { " ($it)" }
                            .orEmpty()

                        Text("${nutrient.displayName}$unitSuffix")
                    },
                    onClick = {
                        expanded = false
                        onNutrientSelected(nutrient.nutrientKey)
                    }
                )
            }
        }
    }
}