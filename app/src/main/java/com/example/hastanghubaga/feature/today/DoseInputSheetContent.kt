package com.example.hastanghubaga.feature.today

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.hastanghubaga.data.local.entity.supplement.SupplementDoseUnit

@Composable
fun DoseInputSheetContent(
    defaultAmount: Double,
    defaultUnit: SupplementDoseUnit,
    onConfirm: (Double, SupplementDoseUnit) -> Unit
) {
    var amount by remember { mutableStateOf(defaultAmount.toString()) }
    var unit by remember { mutableStateOf(defaultUnit) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Log supplement intake", style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(
            value = amount,
            onValueChange = { newValue ->
                amount = newValue
            },
            label = { Text("Amount") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            )
        )

        SupplementUnitDropdown(
            selected = unit,
            onSelected = { unit = it }
        )

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                amount.toDoubleOrNull()?.let {
                    onConfirm(it, unit)
                }
            }
        ) {
            Text("Confirm")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupplementUnitDropdown(
    selected: SupplementDoseUnit,
    onSelected: (SupplementDoseUnit) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selected.name.replace('_', ' ').lowercase()
                .replaceFirstChar { it.uppercase() },
            onValueChange = {},
            readOnly = true,
            label = { Text("Unit") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor(
                    type = MenuAnchorType.PrimaryNotEditable,
                    enabled = true
                )
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            SupplementDoseUnit.entries.forEach { unit ->
                DropdownMenuItem(
                    text = {
                        Text(
                            unit.name.replace('_', ' ').lowercase()
                                .replaceFirstChar { it.uppercase() }
                        )
                    },
                    onClick = {
                        onSelected(unit)
                        expanded = false
                    }
                )
            }
        }
    }
}

