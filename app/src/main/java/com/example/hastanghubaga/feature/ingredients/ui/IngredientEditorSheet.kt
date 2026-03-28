package com.example.hastanghubaga.feature.ingredients.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.hastanghubaga.data.local.entity.supplement.IngredientUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientEditorSheet(
    state: IngredientEditorState,
    onCodeChanged: (String) -> Unit,
    onNameChanged: (String) -> Unit,
    onDefaultUnitChanged: (IngredientUnit) -> Unit,
    onRdaValueChanged: (String) -> Unit,
    onRdaUnitChanged: (IngredientUnit?) -> Unit,
    onUpperLimitValueChanged: (String) -> Unit,
    onUpperLimitUnitChanged: (IngredientUnit?) -> Unit,
    onCategoryChanged: (String) -> Unit,
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
            text = if (state.isEditing) "Edit Ingredient" else "Add Ingredient",
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
            label = { Text("Name") },
            singleLine = true,
            enabled = !state.isSaving
        )

        OutlinedTextField(
            value = state.code,
            onValueChange = onCodeChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Code") },
            singleLine = true,
            enabled = !state.isSaving
        )

        IngredientUnitDropdownField(
            label = "Default unit",
            selectedUnit = state.defaultUnit,
            enabled = !state.isSaving,
            allowNone = false,
            onUnitSelected = { unit ->
                if (unit != null) {
                    onDefaultUnitChanged(unit)
                }
            }
        )

        HorizontalDivider()

        OutlinedTextField(
            value = state.rdaValue,
            onValueChange = onRdaValueChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("RDA value") },
            singleLine = true,
            enabled = !state.isSaving
        )

        IngredientUnitDropdownField(
            label = "RDA unit",
            selectedUnit = state.rdaUnit,
            enabled = !state.isSaving,
            allowNone = true,
            onUnitSelected = onRdaUnitChanged
        )

        HorizontalDivider()

        OutlinedTextField(
            value = state.upperLimitValue,
            onValueChange = onUpperLimitValueChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Upper limit value") },
            singleLine = true,
            enabled = !state.isSaving
        )

        IngredientUnitDropdownField(
            label = "Upper limit unit",
            selectedUnit = state.upperLimitUnit,
            enabled = !state.isSaving,
            allowNone = true,
            onUnitSelected = onUpperLimitUnitChanged
        )

        HorizontalDivider()

        OutlinedTextField(
            value = state.category,
            onValueChange = onCategoryChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Category") },
            singleLine = true,
            enabled = !state.isSaving
        )

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
                    CircularProgressIndicator()
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IngredientUnitDropdownField(
    label: String,
    selectedUnit: IngredientUnit?,
    enabled: Boolean,
    allowNone: Boolean,
    onUnitSelected: (IngredientUnit?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val selectedLabel = selectedUnit?.name ?: "None"

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
            label = { Text(label) },
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
            if (allowNone) {
                DropdownMenuItem(
                    text = { Text("None") },
                    onClick = {
                        expanded = false
                        onUnitSelected(null)
                    }
                )
            }

            IngredientUnit.entries.forEach { unit ->
                DropdownMenuItem(
                    text = { Text(unit.name) },
                    onClick = {
                        expanded = false
                        onUnitSelected(unit)
                    }
                )
            }
        }
    }
}
