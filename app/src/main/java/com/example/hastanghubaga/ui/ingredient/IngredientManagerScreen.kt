package com.example.hastanghubaga.ui.ingredient

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.hastanghubaga.data.local.entity.supplement.IngredientEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientManagerScreen(
    viewModel: IngredientManagerViewModel = hiltViewModel()
) {
    val ingredients by viewModel.ingredients.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ingredients") }
            )
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            items(
                items = ingredients,
                key = { it.id } // ✅ DO NOT use code yet
            ) { ingredient ->

                IngredientRow(ingredient)
            }
        }
    }
}

@Composable
private fun IngredientRow(ingredient: IngredientEntity) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(text = ingredient.name, style = MaterialTheme.typography.titleMedium)

            Spacer(modifier = Modifier.height(4.dp))

            Text(text = "Code: ${ingredient.code}")
            Text(text = "Unit: ${ingredient.defaultUnit}")
            Text(text = "Category: ${ingredient.category ?: "-"}")
        }
    }
}
