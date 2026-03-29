package com.example.hastanghubaga.feature.settings.eventtimes

import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.hastanghubaga.data.local.entity.supplement.DoseAnchorType
import kotlinx.datetime.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefaultEventTimesScreen(
    viewModel: DefaultEventTimesViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val items by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Event Times") }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            TimePickerDialog(
                                context,
                                { _, hourOfDay, minute ->
                                    viewModel.updateTime(
                                        anchor = item.anchor,
                                        newTime = LocalTime(hour = hourOfDay, minute = minute)
                                    )
                                },
                                item.time.hour,
                                item.time.minute,
                                false
                            ).show()
                        },
                    colors = CardDefaults.cardColors()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = item.anchor.toDisplayName(),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = item.time.toDisplayString(),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

private fun LocalTime.toDisplayString(): String {
    val hour12 = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    val amPm = if (hour < 12) "AM" else "PM"
    val minuteText = minute.toString().padStart(2, '0')
    return "$hour12:$minuteText $amPm"
}

private fun com.example.hastanghubaga.data.local.entity.supplement.DoseAnchorType.toDisplayName(): String =
    when (this) {
        DoseAnchorType.MIDNIGHT -> "Midnight"
        DoseAnchorType.WAKEUP -> "Wake Up"
        DoseAnchorType.BREAKFAST -> "Breakfast"
        DoseAnchorType.LUNCH -> "Lunch"
        DoseAnchorType.DINNER -> "Dinner"
        DoseAnchorType.BEFORE_WORKOUT -> "Before Workout"
        DoseAnchorType.AFTER_WORKOUT -> "After Workout"
        DoseAnchorType.SLEEP -> "Sleep"
        DoseAnchorType.ANYTIME -> "Anytime"
        DoseAnchorType.SNACK -> "Snack"
        DoseAnchorType.CAFFEINE -> "Caffeine"
        DoseAnchorType.CUSTOM_EVENT -> "Custom"
        DoseAnchorType.ANY_MEAL -> "Any Meal"
    }
