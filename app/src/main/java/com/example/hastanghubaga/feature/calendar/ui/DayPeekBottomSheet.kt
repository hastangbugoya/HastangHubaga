package com.example.hastanghubaga.feature.calendar.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.hastanghubaga.feature.calendar.model.DaySummaryUi
import kotlinx.datetime.LocalDate

@Composable
fun DayPeekBottomSheet(
    date: LocalDate,
    summary: DaySummaryUi?,
    onOpenDay: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "${date.month.name.lowercase().replaceFirstChar { it.titlecase() }} ${date.dayOfMonth}, ${date.year}",
            style = MaterialTheme.typography.titleMedium
        )

        val supp = summary?.supplementsLogged ?: 0
        val meals = summary?.mealsLogged ?: 0
        val acts = summary?.activitiesCompleted ?: 0

        // Simple “peek” stats (clean and stable)
        Text("Supplements logged: $supp", style = MaterialTheme.typography.bodyMedium)
        Text("Meals logged: $meals", style = MaterialTheme.typography.bodyMedium)
        Text("Activities completed: $acts", style = MaterialTheme.typography.bodyMedium)

        Spacer(Modifier.height(4.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = onOpenDay) {
                Text("Open Day")
            }
            OutlinedButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    }
}

