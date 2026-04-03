package com.example.hastanghubaga.feature.activities.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.hastanghubaga.R

data class ActivityListItemUi(
    val id: Long,
    val typeLabel: String,
    val notes: String?,
    val intensityLabel: String?,
    val startLabel: String,
    val isActive: Boolean = true,
    val hasSchedule: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivitiesScreen(
    items: List<ActivityListItemUi>,
    onAddClick: () -> Unit,
    onItemClick: (Long) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Activities") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Text("+")
            }
        }
    ) { innerPadding ->
        if (items.isEmpty()) {
            EmptyActivitiesState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items, key = { it.id }) { item ->
                    ActivityRow(
                        item = item,
                        onClick = { onItemClick(item.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyActivitiesState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "No activities yet",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = "Create your first activity entry to start managing activity data in Hastang.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp)
        )
        Text(
            text = "Tap + to add one.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}

@Composable
private fun ActivityRow(
    item: ActivityListItemUi,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.typeLabel,
                    style = MaterialTheme.typography.titleMedium
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(
                            if (item.isActive) R.drawable.check else R.drawable.cross_small
                        ),
                        contentDescription = if (item.isActive) "Active" else "Inactive",
                        tint = if (item.isActive) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        }
                    )

                    Icon(
                        painter = painterResource(R.drawable.time_watch_calendar),
                        contentDescription = if (item.hasSchedule) "Scheduled" else "Not scheduled",
                        tint = if (item.hasSchedule) {
                            MaterialTheme.colorScheme.tertiary
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        }
                    )

                    Text(
                        text = item.startLabel,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            val intensity = item.intensityLabel
            if (intensity != null) {
                Text(
                    text = intensity,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            val notes = item.notes?.takeIf { it.isNotBlank() }
            if (notes != null) {
                Text(
                    text = notes,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}