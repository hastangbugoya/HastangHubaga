package com.example.hastanghubaga.ui.screens

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.hastanghubaga.ui.components.BasicScreenTopBar

private data class SettingsDestinationUi(
    val title: String,
    val subtitle: String,
    val onClick: () -> Unit
)

@Composable
fun SettingsScreen(
    onImportFromAdobongKangkong: () -> Unit,
    onOpenSupplements: () -> Unit,
    onOpenNutritionGoals: () -> Unit,
    onOpenNutrients: () -> Unit,
    onOpenActivities: () -> Unit,
    onOpenIngredients: () -> Unit,
    onOpenMeals: () -> Unit,
    onOpenEventTimes: () -> Unit
) {
    val items = listOf(
        SettingsDestinationUi(
            title = "Import from AdobongKangkong",
            subtitle = "Read the latest logged food items and import them into HastangHubaga",
            onClick = onImportFromAdobongKangkong
        ),
        SettingsDestinationUi(
            title = "Nutrition Goals",
            subtitle = "Create and manage nutrition plans and nutrient goal targets",
            onClick = onOpenNutritionGoals
        ),
        SettingsDestinationUi(
            title = "Event Times",
            subtitle = "Edit default anchor times (wake up, meals, sleep, etc.)",
            onClick = onOpenEventTimes
        ),
        SettingsDestinationUi(
            title = "Supplements",
            subtitle = "Create and manage supplement entries",
            onClick = onOpenSupplements
        ),
        SettingsDestinationUi(
            title = "Meals",
            subtitle = "Create and manage meal entries",
            onClick = onOpenMeals
        ),
        SettingsDestinationUi(
            title = "Nutrients",
            subtitle = "Edit tracked nutrients and metadata",
            onClick = onOpenNutrients
        ),
        SettingsDestinationUi(
            title = "Activities",
            subtitle = "Manage activity types and defaults",
            onClick = onOpenActivities
        ),
        SettingsDestinationUi(
            title = "Ingredients",
            subtitle = "Open the existing ingredient manager",
            onClick = onOpenIngredients
        )
    )

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        BasicScreenTopBar(
            title = "Settings"
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = item.onClick),
                    colors = CardDefaults.cardColors()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = item.subtitle,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}