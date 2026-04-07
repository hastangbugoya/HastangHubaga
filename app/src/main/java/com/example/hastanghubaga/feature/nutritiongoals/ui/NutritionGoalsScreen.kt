package com.example.hastanghubaga.feature.nutritiongoals.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NutritionGoalsScreen(
    items: List<NutritionPlanListItemUi>,
    onAddClick: () -> Unit,
    onItemClick: (Long) -> Unit,
    onTogglePlanActive: (Long, Boolean) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Nutrition Goals") },
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
            EmptyNutritionGoalsState(
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
                    NutritionPlanRow(
                        item = item,
                        onClick = { onItemClick(item.id) },
                        onTogglePlanActive = { isActive ->
                            onTogglePlanActive(item.id, isActive)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyNutritionGoalsState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "No nutrition plans yet",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = "Create your first nutrition plan to manage goal targets in HastangHubaga.",
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
private fun NutritionPlanRow(
    item: NutritionPlanListItemUi,
    onClick: () -> Unit,
    onTogglePlanActive: (Boolean) -> Unit
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = item.typeLabel,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Switch(
                    checked = item.isActive,
                    onCheckedChange = onTogglePlanActive
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(
                    onClick = {},
                    enabled = false,
                    label = {
                        Text(
                            text = if (item.isActive) "Enabled" else "Disabled"
                        )
                    }
                )

                AssistChip(
                    onClick = {},
                    enabled = false,
                    label = {
                        Text(
                            text = item.sourceType
                        )
                    }
                )

                AssistChip(
                    onClick = {},
                    enabled = false,
                    label = {
                        Text(
                            text = if (item.endDate == null) "Ongoing" else "Has end date"
                        )
                    }
                )
            }

            Text(
                text = buildDateRangeText(
                    startDate = item.startDate,
                    endDate = item.endDate
                ),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

private fun buildDateRangeText(
    startDate: Long,
    endDate: Long?
): String {
    return if (endDate == null) {
        "Start: $startDate"
    } else {
        "Start: $startDate • End: $endDate"
    }
}
