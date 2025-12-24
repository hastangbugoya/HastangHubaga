package com.example.hastanghubaga.feature.today

import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hastanghubaga.ui.common.BannerController
import com.example.hastanghubaga.ui.common.BottomSheetController
import com.example.hastanghubaga.ui.common.ErrorView
import com.example.hastanghubaga.ui.common.LoadingView
import com.example.hastanghubaga.ui.common.SnackbarController
import com.example.hastanghubaga.ui.preview.PreviewData
import com.example.hastanghubaga.ui.timeline.TimelineItemUiModel
import com.example.hastanghubaga.ui.timeline.toPreviewTimelineItems
import com.example.hastanghubaga.ui.timeline.toTimelineItemUiModel
import com.example.hastanghubaga.ui.tokens.Dimens
import com.example.hastanghubaga.ui.tokens.UiColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    snackbarController: SnackbarController,
    bannerController: BannerController,
    bottomSheetController: BottomSheetController,
    onNavigate: (TodayScreenContract.Destination) -> Unit,
    viewModel: TodayScreenViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // 🔴 Today-local bottom sheet state
    var doseSheetData by remember {
        mutableStateOf<TodayScreenContract.Effect.ShowDoseInputDialog?>(null)
    }
    val doseSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    LaunchedEffect(
        state.isLoading,
        state.uiTimelineItems.size
    ) {
        if (!state.isLoading && state.uiTimelineItems.isNotEmpty()) {
            val count = state.uiTimelineItems.size
            bannerController.show(
                if (count == 1)
                    "Loaded 1 timeline item"
                else
                    "Loaded $count timeline items"
            )
        }
    }

    LaunchedEffect(Unit) {
        viewModel.onIntent(TodayScreenContract.Intent.LoadToday)
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is TodayScreenContract.Effect.ShowSnackbar ->
                    snackbarController.show(effect.message)

                is TodayScreenContract.Effect.ShowBanner ->
                    bannerController.show(effect.message)

                is TodayScreenContract.Effect.ShowBottomSheet ->
                    bottomSheetController.show(effect.content)

                is TodayScreenContract.Effect.Navigate ->
                    onNavigate(effect.destination)

                is TodayScreenContract.Effect.ShowError ->
                    bannerController.show(effect.message)

                is TodayScreenContract.Effect.ShowDoseInputDialog -> {
                    doseSheetData = effect
                }

            }
        }
    }

    TodayScreenContent(
        state = state,
        onItemClick = {
            viewModel.onIntent(
                TodayScreenContract.Intent.TimelineItemClicked(it)
            )
        },
        onRefresh = {
            viewModel.onIntent(TodayScreenContract.Intent.Refresh)
        }
    )
    doseSheetData?.let { data ->
        ModalBottomSheet(
            sheetState = doseSheetState,
            onDismissRequest = { doseSheetData = null }
        ) {
            DoseInputSheetContent(
                defaultAmount = data.suggestedDose,
                defaultUnit = data.defaultUnit,
                onConfirm = { amount, unit ->
                    viewModel.onIntent(
                        TodayScreenContract.Intent.ConfirmDose(
                            supplementId = data.supplementId,
                            amount = amount,
                            unit = unit,
                            scheduledTime = data.scheduledTime,
                            actualTime = null
                        )
                    )
                    doseSheetData = null
                }
            )
        }
    }
}

@Composable
fun TimelineList(
    items: List<TimelineItemUiModel>,
    onItemClick: (TimelineItemUiModel) -> Unit
) {
    Log.d("TimelineList", "Rendering ${items.size} items")
    items.forEach {
        Log.d("TimelineList", "Rendering item: ${it.title} key:${it.key}")
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(
            items = items,
            key = { it.key } // stable UI key (correct)
        ) { item ->
            TimelineRow(
                item = item,
                onClick = onItemClick
            )
        }
    }
}

@Composable
fun TodayScreenContent(
    state: TodayScreenContract.State,
    onItemClick: (TimelineItemUiModel) -> Unit,
    onRefresh: () -> Unit
) {
    when {
        state.isLoading -> LoadingView()

        state.errorMessage != null ->
            ErrorView(state.errorMessage)

        else ->
            TimelineList(
                items = state.uiTimelineItems,
                onItemClick = onItemClick
            )
    }
}

@Composable
fun TimelineRow(
    item: TimelineItemUiModel,
    onClick: (TimelineItemUiModel) -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dimens.SpaceS)
            .border(color = UiColors.Primary(), width = 1.dp)
            .padding(Dimens.SpaceS)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                enabled = true,
                onClick = { onClick(item) }
            ),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = item.time.toString(), style = MaterialTheme.typography.titleMedium)
            Text(text = item.title, style = MaterialTheme.typography.titleLarge)
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
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn {
                items(
                    items = uiItems,
                    key = {it.key}
                ) { item ->
                    TimelineRow(item)
                }
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
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn {
                items(uiItems) { item ->
                    TimelineRow(item)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TimelineListPreview() {
    val uiItems =
        PreviewData.supplementList
            .flatMap { it.toPreviewTimelineItems() }
            .map { it.toTimelineItemUiModel() }
            .sortedBy { it.time }

    MaterialTheme {
        TimelineList(
            items = uiItems,
            onItemClick = {}
        )
    }
}