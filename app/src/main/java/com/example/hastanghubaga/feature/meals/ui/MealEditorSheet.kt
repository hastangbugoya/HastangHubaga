package com.example.hastanghubaga.feature.meals.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.hastanghubaga.data.local.entity.meal.MealType
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealEditorSheet(
    state: MealEditorUiState,
    onTypeChanged: (MealType) -> Unit,
    onNotesChanged: (String) -> Unit,
    onTimestampChanged: (Long) -> Unit,
    onSaveClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isExisting = !state.isNew
    var typeMenuExpanded by remember { mutableStateOf(false) }

    var hourText by remember(state.timestampMillis) {
        mutableStateOf(timestampTo12Hour(state.timestampMillis).toString())
    }
    var minuteText by remember(state.timestampMillis) {
        mutableStateOf(timestampToMinute(state.timestampMillis).toString().padStart(2, '0'))
    }
    var amPm by remember(state.timestampMillis) {
        mutableStateOf(timestampToAmPm(state.timestampMillis))
    }
    var amPmExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(hourText, minuteText, amPm) {
        val parsedHour = hourText.toIntOrNull()
        val parsedMinute = minuteText.toIntOrNull()

        if (parsedHour != null &&
            parsedMinute != null &&
            parsedHour in 1..12 &&
            parsedMinute in 0..59
        ) {
            val updatedTimestamp = mergeTimeIntoTimestamp(
                baseTimestampMillis = state.timestampMillis,
                hour12 = parsedHour,
                minute = parsedMinute,
                amPm = amPm
            )
            if (updatedTimestamp != state.timestampMillis) {
                onTimestampChanged(updatedTimestamp)
            }
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
            text = if (state.isNew) "Add meal" else "Edit meal",
            style = MaterialTheme.typography.titleLarge
        )

        Text(
            text = "Meals are simple planned timeline items for now. Set a time on the intended day so the meal can appear in the day timeline.",
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
                label = { Text("Meal type") },
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
                MealType.values().forEach { type ->
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

        Text(
            text = "Scheduled time",
            style = MaterialTheme.typography.titleMedium
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = hourText,
                onValueChange = { value ->
                    if (value.length <= 2 && value.all(Char::isDigit)) {
                        hourText = value
                    }
                },
                modifier = Modifier.weight(1f),
                label = { Text("Hour") },
                singleLine = true
            )

            OutlinedTextField(
                value = minuteText,
                onValueChange = { value ->
                    if (value.length <= 2 && value.all(Char::isDigit)) {
                        minuteText = value
                    }
                },
                modifier = Modifier.weight(1f),
                label = { Text("Minute") },
                singleLine = true
            )

            ExposedDropdownMenuBox(
                expanded = amPmExpanded,
                onExpandedChange = { amPmExpanded = !amPmExpanded }
            ) {
                OutlinedTextField(
                    value = amPm,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("AM/PM") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = amPmExpanded)
                    },
                    modifier = Modifier
                        .width(120.dp)
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = amPmExpanded,
                    onDismissRequest = { amPmExpanded = false }
                ) {
                    listOf("AM", "PM").forEach { meridiem ->
                        DropdownMenuItem(
                            text = { Text(meridiem) },
                            onClick = {
                                amPm = meridiem
                                amPmExpanded = false
                            }
                        )
                    }
                }
            }
        }

        Text(
            text = "This keeps the current meal date and updates only the time.",
            style = MaterialTheme.typography.bodySmall
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
                Text("Delete meal")
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

private fun MealType.toDisplayLabel(): String =
    name
        .lowercase()
        .split("_")
        .joinToString(" ") { word -> word.replaceFirstChar { it.uppercase() } }

private fun timestampTo12Hour(timestampMillis: Long): Int {
    val localDateTime = Instant.ofEpochMilli(timestampMillis)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()

    val hour24 = localDateTime.hour
    return when {
        hour24 == 0 -> 12
        hour24 > 12 -> hour24 - 12
        else -> hour24
    }
}

private fun timestampToMinute(timestampMillis: Long): Int =
    Instant.ofEpochMilli(timestampMillis)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
        .minute

private fun timestampToAmPm(timestampMillis: Long): String {
    val hour24 = Instant.ofEpochMilli(timestampMillis)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
        .hour

    return if (hour24 < 12) "AM" else "PM"
}

private fun mergeTimeIntoTimestamp(
    baseTimestampMillis: Long,
    hour12: Int,
    minute: Int,
    amPm: String
): Long {
    val zoneId = ZoneId.systemDefault()
    val base = Instant.ofEpochMilli(baseTimestampMillis)
        .atZone(zoneId)

    val hour24 = when {
        amPm == "AM" && hour12 == 12 -> 0
        amPm == "AM" -> hour12
        amPm == "PM" && hour12 == 12 -> 12
        else -> hour12 + 12
    }

    return base
        .withHour(hour24)
        .withMinute(minute)
        .withSecond(0)
        .withNano(0)
        .toInstant()
        .toEpochMilli()
}