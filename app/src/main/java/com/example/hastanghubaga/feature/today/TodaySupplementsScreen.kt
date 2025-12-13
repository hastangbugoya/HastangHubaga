package com.example.hastanghubaga.feature.today

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.room.util.TableInfo
import com.example.hastanghubaga.data.local.entity.supplement.SupplementEntity
import com.example.hastanghubaga.domain.model.supplement.Supplement

@Composable
fun TodaySupplementsScreen(
    showBottomSheet: (content: @Composable () -> Unit) -> Unit,
    snackbarData: SnackbarHostState,
    viewModel: TodaySupplementsViewModel
) {
    val state by viewModel.state.collectAsState()
    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Button(onClick = {
                showBottomSheet {
                    Text("Hello from Today Screen!")
                }
            }) {
                Text("Show Alert Sheet")
            }
        }
        Box(modifier = Modifier.fillMaxSize()){
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
    }
}

@Composable
private fun SupplementRow(supplement: Supplement) {
    Text(supplement.name)
}
