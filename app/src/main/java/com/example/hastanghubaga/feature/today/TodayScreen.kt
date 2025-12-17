package com.example.hastanghubaga.feature.today

import android.content.res.Configuration
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.hastanghubaga.ui.preview.PreviewData
import com.example.hastanghubaga.ui.timeline.TimelineItemUiModel
import com.example.hastanghubaga.ui.timeline.toPreviewTimelineItems
import com.example.hastanghubaga.ui.timeline.toTimelineItemUiModel
import com.example.hastanghubaga.ui.tokens.Dimens
import com.example.hastanghubaga.ui.tokens.UiColors

@Composable
fun TodayScreen(
    showBottomSheet: (content: @Composable () -> Unit) -> Unit,
    snackbarData: SnackbarHostState,
    viewModel: TodayScreenViewModel = hiltViewModel()
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
                        items(state.timelineItems, key = {it.key}) { supp ->
                            TimelineRow(supp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TimelineRow(
    item: TimelineItemUiModel
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dimens.SpaceS)
            .border(color = UiColors.Primary(), width = 1.dp)
            .padding(Dimens.SpaceS)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            Text(text = item.time.toString())
            Text(text = item.title)

            item.subtitle?.let { Text(text = it) }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TimelineRowPreview() {
    val uiItems =
        PreviewData.supplementList
            .flatMap { it.toPreviewTimelineItems() }
            .map { it.toTimelineItemUiModel() }
            .sortedBy { it.time }

    MaterialTheme {
        LazyColumn {
            items(uiItems) { item ->
                TimelineRow(item)
            }
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SupplementRowPreviewDark() {
    val uiItems =
        PreviewData.supplementList
            .flatMap { it.toPreviewTimelineItems() }
            .map { it.toTimelineItemUiModel() }
            .sortedBy { it.time }

    MaterialTheme {
        LazyColumn {
            items(uiItems) { item ->
                TimelineRow(item)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TimelineListPreview() {
    val timelineItems =
        PreviewData.supplementList
            .flatMap { it.toPreviewTimelineItems() }
            .sortedBy { it.time }

    MaterialTheme {
        LazyColumn {
            items(
                items = timelineItems,
                key = { it.toTimelineItemUiModel().id }
            ) { item ->
                TimelineRow(item.toTimelineItemUiModel())
            }
        }
    }
}