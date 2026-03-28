package com.example.hastanghubaga.feature.ingredients.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
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
import androidx.compose.ui.unit.dp
import com.example.hastanghubaga.data.local.entity.supplement.IngredientEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientsScreen(
    items: List<IngredientEntity>,
    onAddClick: () -> Unit,
    onItemClick: (Long) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Ingredients") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add ingredient"
                )
            }
        }
    ) { paddingValues ->
        if (items.isEmpty()) {
            IngredientsEmptyState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(
                    start = 12.dp,
                    end = 12.dp,
                    top = 12.dp,
                    bottom = 88.dp
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = items,
                    key = { it.id }
                ) { ingredient ->
                    IngredientRow(
                        ingredient = ingredient,
                        onClick = { onItemClick(ingredient.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun IngredientsEmptyState(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No ingredients yet.",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun IngredientRow(
    ingredient: IngredientEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = ingredient.name,
                style = MaterialTheme.typography.titleMedium
            )

            if (ingredient.code.isNotBlank()) {
                Text(
                    text = "Code: ${ingredient.code}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Text(
                text = "Unit: ${ingredient.defaultUnit}",
                style = MaterialTheme.typography.bodyMedium
            )

            ingredient.category?.takeIf { it.isNotBlank() }?.let { category ->
                Text(
                    text = "Category: $category",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}