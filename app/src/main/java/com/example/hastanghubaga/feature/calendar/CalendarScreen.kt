package com.example.hastanghubaga.feature.calendar

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.OpenDocument
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hastanghubaga.feature.calendar.ui.DayPeekBottomSheet
import com.example.hastanghubaga.feature.calendar.ui.MonthCalendarPanel
import kotlinx.datetime.LocalDate

private const val PREFS_NAME = "adobo_snapshot_prefs"
private const val KEY_ADOBO_SNAPSHOT_URI = "adobo_snapshot_uri"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState,
    onNavigateToDate: (LocalDate) -> Unit,
    viewModel: CalendarViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var dayPeekDate by remember { mutableStateOf<LocalDate?>(null) }
    var showDayPeek by remember { mutableStateOf(false) }

    val savedUri = remember {
        mutableStateOf(loadAdoboSnapshotUri(context))
    }

    val documentLauncher = rememberLauncherForActivityResult(
        contract = OpenDocument()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult

        try {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (e: SecurityException) {
            Log.w("AdoboRead", "Persistable permission not granted", e)
        }

        saveAdoboSnapshotUri(context, uri)
        savedUri.value = uri
        viewModel.readAdoboSnapshot(context, uri)
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is CalendarContract.Effect.NavigateToDate -> onNavigateToDate(effect.date)

                is CalendarContract.Effect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }

                CalendarContract.Effect.CloseDayPeek -> {
                    try {
                        sheetState.hide()
                    } catch (e: kotlinx.coroutines.CancellationException) {
                        throw e
                    } finally {
                        showDayPeek = false
                        dayPeekDate = null
                    }
                }

                is CalendarContract.Effect.OpenDayPeek -> {
                    dayPeekDate = effect.date
                    showDayPeek = true
                }
            }
        }
    }

    Box(modifier = modifier) {
        if (showDayPeek && dayPeekDate != null) {
            val date = dayPeekDate!!
            val summary = state.summaries[date]

            ModalBottomSheet(
                onDismissRequest = { viewModel.onEvent(CalendarContract.Event.DayPeekDismissed) },
                sheetState = sheetState
            ) {
                DayPeekBottomSheet(
                    date = date,
                    summary = summary,
                    onOpenDay = { viewModel.onEvent(CalendarContract.Event.OpenDayClicked(date)) },
                    onDismiss = { viewModel.onEvent(CalendarContract.Event.DayPeekDismissed) }
                )
                Spacer(Modifier.height(12.dp))
            }
        }

        MonthCalendarPanel(
            modifier = Modifier.matchParentSize(),
            month = state.month,
            selectedDate = state.selectedDate,
            summaries = state.summaries,
            onPrevMonth = { viewModel.onEvent(CalendarContract.Event.PrevMonthClicked) },
            onNextMonth = { viewModel.onEvent(CalendarContract.Event.NextMonthClicked) },
            onToday = { viewModel.onEvent(CalendarContract.Event.TodayClicked) },
            onDateClick = { viewModel.onEvent(CalendarContract.Event.DateClicked(it)) },
        )

        if (state.adoboSnapshot != null || state.isLoading) {
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp),
                colors = CardDefaults.cardColors()
            ) {
                Box(
                    modifier = Modifier.padding(12.dp)
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator()
                    } else {
                        val snapshot = state.adoboSnapshot
                        if (snapshot != null) {
                            Text(
                                text = buildString {
                                    append("Adobo Snapshot")
                                    append("\nDate: ${snapshot.dateIso}")
                                    append("\nCalories: ${snapshot.calories ?: "-"}")
                                    append("\nProtein: ${snapshot.protein ?: "-"}")
                                    append("\nCarbs: ${snapshot.carbs ?: "-"}")
                                    append("\nFat: ${snapshot.fat ?: "-"}")
                                },
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }

        ExtendedFloatingActionButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            onClick = {
                val uri = savedUri.value
                if (uri != null) {
                    viewModel.readAdoboSnapshot(context, uri)
                } else {
                    documentLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
                }
            }
        ) {
            Text(
                text = if (savedUri.value == null) {
                    "Link Adobo JSON"
                } else {
                    "Read Adobo JSON"
                }
            )
        }
    }
}

private fun saveAdoboSnapshotUri(context: Context, uri: Uri) {
    context
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putString(KEY_ADOBO_SNAPSHOT_URI, uri.toString())
        .apply()
}

private fun loadAdoboSnapshotUri(context: Context): Uri? {
    val raw = context
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getString(KEY_ADOBO_SNAPSHOT_URI, null)

    return raw?.let(Uri::parse)
}