package com.example.hastanghubaga.feature.today

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.hastanghubaga.data.local.entity.supplement.SupplementEntity

@Composable
fun TodaySupplementsScreen(
    viewModel: TodaySupplementsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    when {
        state.isLoading -> {
            Text("Loading...")
        }
        state.errorMessage != null -> {
            Text("Error: ${state.errorMessage}")
        }
        else -> {
            LazyColumn {
                items(state.todaySupplements) { supp ->
                    SupplementRow(supp)
                }
            }
        }
    }
}

@Composable
private fun SupplementRow(supplement: SupplementEntity) {
    Text(supplement.name)
}
