package com.example.hastanghubaga.feature.schedule.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.hastanghubaga.feature.schedule.ui.model.AnchorTypeUi
import com.example.hastanghubaga.feature.schedule.ui.model.AnchoredTimeRowUi
import com.example.hastanghubaga.feature.schedule.ui.model.FixedTimeRowUi
import com.example.hastanghubaga.feature.schedule.ui.model.RecurrenceMode
import com.example.hastanghubaga.feature.schedule.ui.model.ScheduleEditorState
import com.example.hastanghubaga.feature.schedule.ui.model.ScheduleValidationError
import com.example.hastanghubaga.feature.schedule.ui.model.TimingMode
import com.example.hastanghubaga.feature.schedule.ui.model.WeekdayUi
import kotlinx.datetime.LocalDate

@Composable
fun ScheduleEditorSection(
    state: ScheduleEditorState,
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
    modifier: Modifier = Modifier,
    title: String = "Schedule"
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            HeaderRow(
                title = title,
                isEnabled = state.isEnabled,
                onEnabledChanged = onEnabledChanged
            )

            HorizontalDivider()

            RecurrenceSection(
                state = state,
                onRecurrenceModeChanged = onRecurrenceModeChanged,
                onIntervalInputChanged = onIntervalInputChanged,
                onWeekdayToggled = onWeekdayToggled
            )

            HorizontalDivider()

            DateWindowSection(
                startDate = state.startDate,
                hasEndDate = state.hasEndDate,
                endDate = state.endDate,
                onStartDateClick = onStartDateClick,
                onEndDateToggleChanged = onEndDateToggleChanged,
                onEndDateClick = onEndDateClick
            )

            HorizontalDivider()

            TimingSection(
                state = state,
                onTimingModeChanged = onTimingModeChanged,
                onFixedTimeChanged = onFixedTimeChanged,
                onAddFixedTime = onAddFixedTime,
                onRemoveFixedTime = onRemoveFixedTime,
                onAnchoredRowAnchorChanged = onAnchoredRowAnchorChanged,
                onAnchoredRowOffsetChanged = onAnchoredRowOffsetChanged,
                onAddAnchoredRow = onAddAnchoredRow,
                onRemoveAnchoredRow = onRemoveAnchoredRow
            )

            ValidationSection(errors = state.validationErrors)

            SummarySection(summaryText = state.summaryText)
        }
    }
}

@Composable
private fun HeaderRow(
    title: String,
    isEnabled: Boolean,
    onEnabledChanged: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title)
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = if (isEnabled) "Enabled" else "Disabled")
            Switch(
                checked = isEnabled,
                onCheckedChange = onEnabledChanged
            )
        }
    }
}

@Composable
private fun RecurrenceSection(
    state: ScheduleEditorState,
    onRecurrenceModeChanged: (RecurrenceMode) -> Unit,
    onIntervalInputChanged: (String) -> Unit,
    onWeekdayToggled: (WeekdayUi) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(text = "Recurrence")

        ModeRadioRow(
            options = RecurrenceMode.entries,
            selected = state.recurrenceMode,
            label = { it.name.lowercase().replaceFirstChar(Char::uppercase) },
            onSelected = onRecurrenceModeChanged
        )

        OutlinedTextField(
            value = state.intervalInput,
            onValueChange = onIntervalInputChanged,
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text(
                    text = when (state.recurrenceMode) {
                        RecurrenceMode.DAILY -> "Every N days"
                        RecurrenceMode.WEEKLY -> "Every N weeks"
                    }
                )
            },
            singleLine = true
        )

        if (state.recurrenceMode == RecurrenceMode.WEEKLY) {
            WeekdaySelector(
                selectedWeekdays = state.selectedWeekdays,
                onWeekdayToggled = onWeekdayToggled
            )
        }
    }
}

@Composable
private fun DateWindowSection(
    startDate: LocalDate?,
    hasEndDate: Boolean,
    endDate: LocalDate?,
    onStartDateClick: () -> Unit,
    onEndDateToggleChanged: (Boolean) -> Unit,
    onEndDateClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(text = "Date Range")

        OutlinedButton(
            onClick = onStartDateClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Start date: ${startDate?.toString() ?: "Select date"}"
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Has end date")
            Switch(
                checked = hasEndDate,
                onCheckedChange = onEndDateToggleChanged
            )
        }

        if (hasEndDate) {
            OutlinedButton(
                onClick = onEndDateClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "End date: ${endDate?.toString() ?: "Select date"}"
                )
            }
        }
    }
}

@Composable
private fun TimingSection(
    state: ScheduleEditorState,
    onTimingModeChanged: (TimingMode) -> Unit,
    onFixedTimeChanged: (rowId: String, value: String) -> Unit,
    onAddFixedTime: () -> Unit,
    onRemoveFixedTime: (rowId: String) -> Unit,
    onAnchoredRowAnchorChanged: (rowId: String, anchor: AnchorTypeUi) -> Unit,
    onAnchoredRowOffsetChanged: (rowId: String, value: String) -> Unit,
    onAddAnchoredRow: () -> Unit,
    onRemoveAnchoredRow: (rowId: String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(text = "Timing")

        ModeRadioRow(
            options = TimingMode.entries,
            selected = state.timingMode,
            label = { it.name.lowercase().replaceFirstChar(Char::uppercase) },
            onSelected = onTimingModeChanged
        )

        when (state.timingMode) {
            TimingMode.FIXED -> FixedTimesEditor(
                rows = state.fixedTimes,
                onTimeChanged = onFixedTimeChanged,
                onAddRow = onAddFixedTime,
                onRemoveRow = onRemoveFixedTime
            )

            TimingMode.ANCHORED -> AnchoredTimesEditor(
                rows = state.anchoredTimes,
                onAnchorChanged = onAnchoredRowAnchorChanged,
                onOffsetChanged = onAnchoredRowOffsetChanged,
                onAddRow = onAddAnchoredRow,
                onRemoveRow = onRemoveAnchoredRow
            )
        }
    }
}

@Composable
private fun FixedTimesEditor(
    rows: List<FixedTimeRowUi>,
    onTimeChanged: (rowId: String, value: String) -> Unit,
    onAddRow: () -> Unit,
    onRemoveRow: (rowId: String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = row.timeInput,
                    onValueChange = { onTimeChanged(row.id, it) },
                    modifier = Modifier.weight(1f),
                    label = { Text("Time (HH:mm)") },
                    singleLine = true
                )

                TextButton(
                    onClick = { onRemoveRow(row.id) }
                ) {
                    Text("Remove")
                }
            }
        }

        Button(
            onClick = onAddRow
        ) {
            Text("Add time")
        }
    }
}

@Composable
private fun AnchoredTimesEditor(
    rows: List<AnchoredTimeRowUi>,
    onAnchorChanged: (rowId: String, anchor: AnchorTypeUi) -> Unit,
    onOffsetChanged: (rowId: String, value: String) -> Unit,
    onAddRow: () -> Unit,
    onRemoveRow: (rowId: String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        rows.forEach { row ->
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "Anchor")

                AnchorSelector(
                    selected = row.anchor,
                    onSelected = { onAnchorChanged(row.id, it) }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = row.offsetMinutesInput,
                        onValueChange = { onOffsetChanged(row.id, it) },
                        modifier = Modifier.weight(1f),
                        label = { Text("Offset minutes") },
                        singleLine = true
                    )

                    TextButton(
                        onClick = { onRemoveRow(row.id) }
                    ) {
                        Text("Remove")
                    }
                }
            }
        }

        Button(
            onClick = onAddRow
        ) {
            Text("Add anchor time")
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WeekdaySelector(
    selectedWeekdays: Set<WeekdayUi>,
    onWeekdayToggled: (WeekdayUi) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = "Days of week")

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            WeekdayUi.entries.forEach { weekday ->
                AssistChip(
                    onClick = { onWeekdayToggled(weekday) },
                    label = { Text(weekday.shortLabel()) }
                )
            }
        }

        if (selectedWeekdays.isNotEmpty()) {
            Text(
                text = "Selected: ${
                    WeekdayUi.entries
                        .filter { it in selectedWeekdays }
                        .joinToString(", ") { it.shortLabel() }
                }"
            )
        }
    }
}

@Composable
private fun AnchorSelector(
    selected: AnchorTypeUi,
    onSelected: (AnchorTypeUi) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AnchorTypeUi.entries.forEach { anchor ->
            AssistChip(
                onClick = { onSelected(anchor) },
                label = { Text(anchor.label()) }
            )
        }
    }
}

@Composable
private fun ValidationSection(
    errors: List<ScheduleValidationError>
) {
    if (errors.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = "Validation")

        errors.distinct().forEach { error ->
            Text(text = "• ${error.toDisplayText()}")
        }
    }
}

@Composable
private fun SummarySection(
    summaryText: String
) {
    if (summaryText.isBlank()) return

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = "Summary")
        Text(text = summaryText)
    }
}

@Composable
private fun <T> ModeRadioRow(
    options: List<T>,
    selected: T,
    label: (T) -> String,
    onSelected: (T) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        options.forEach { option ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = option == selected,
                        onClick = { onSelected(option) }
                    )
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = option == selected,
                    onClick = { onSelected(option) }
                )
                Text(
                    text = label(option),
                    modifier = Modifier.clickable { onSelected(option) }
                )
            }
        }
    }
}

private fun WeekdayUi.shortLabel(): String {
    return when (this) {
        WeekdayUi.MONDAY -> "Mon"
        WeekdayUi.TUESDAY -> "Tue"
        WeekdayUi.WEDNESDAY -> "Wed"
        WeekdayUi.THURSDAY -> "Thu"
        WeekdayUi.FRIDAY -> "Fri"
        WeekdayUi.SATURDAY -> "Sat"
        WeekdayUi.SUNDAY -> "Sun"
    }
}

private fun AnchorTypeUi.label(): String {
    return when (this) {
        AnchorTypeUi.WAKE_UP -> "Wake Up"
        AnchorTypeUi.BREAKFAST -> "Breakfast"
        AnchorTypeUi.LUNCH -> "Lunch"
        AnchorTypeUi.DINNER -> "Dinner"
        AnchorTypeUi.SLEEP -> "Sleep"
    }
}

private fun ScheduleValidationError.toDisplayText(): String {
    return when (this) {
        ScheduleValidationError.MissingStartDate -> "Start date is required."
        ScheduleValidationError.InvalidInterval -> "Interval must be a number greater than 0."
        ScheduleValidationError.NoWeekdaysSelected -> "Select at least one weekday."
        ScheduleValidationError.NoTimesDefined -> "Add at least one time."
        is ScheduleValidationError.InvalidTimeFormat -> "One or more fixed times are invalid. Use HH:mm."
        is ScheduleValidationError.InvalidOffset -> "One or more anchor offsets are invalid."
        ScheduleValidationError.EndDateBeforeStartDate -> "End date cannot be before start date."
    }
}