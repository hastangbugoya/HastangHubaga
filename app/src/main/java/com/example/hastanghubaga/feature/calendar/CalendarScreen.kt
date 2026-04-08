package com.example.hastanghubaga.feature.calendar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hastanghubaga.feature.calendar.ui.DayPeekBottomSheet
import com.example.hastanghubaga.feature.calendar.ui.MonthCalendarPanel
import com.example.hastanghubaga.ui.components.BasicScreenTopBar
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState,
    onNavigateToDate: (LocalDate) -> Unit,
    viewModel: CalendarViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var dayPeekDate by remember { mutableStateOf<LocalDate?>(null) }
    var showDayPeek by remember { mutableStateOf(false) }

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

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        BasicScreenTopBar(
            title = "Calendar"
        )

        MonthCalendarPanel(
            modifier = Modifier.fillMaxSize(),
            month = state.month,
            selectedDate = state.selectedDate,
            summaries = state.summaries,
            onPrevMonth = { viewModel.onEvent(CalendarContract.Event.PrevMonthClicked) },
            onNextMonth = { viewModel.onEvent(CalendarContract.Event.NextMonthClicked) },
            onToday = { viewModel.onEvent(CalendarContract.Event.TodayClicked) },
            onDateClick = { viewModel.onEvent(CalendarContract.Event.DateClicked(it)) },
        )
    }

    if (showDayPeek && dayPeekDate != null) {
        val date = dayPeekDate!!
        val summary = state.summaries[date]
        val adoboSnapshotForDate =
            state.adoboSnapshot?.takeIf { it.dateIso == date.toString() }
        val importedMealsForDate = state.importedMealsForSelectedDate

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

            if (importedMealsForDate.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Imported AK meals",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(Modifier.height(4.dp))

                importedMealsForDate.forEach { meal ->
                    Text(
                        text = buildString {
                            append(meal.type.name.replace("_", " "))
                            meal.notes?.takeIf { it.isNotBlank() }?.let {
                                append(": ")
                                append(it)
                            }
                            append("\nCalories: ${meal.calories}")
                            append("\nProtein: ${meal.protein}")
                            append("\nCarbs: ${meal.carbs}")
                            append("\nFat: ${meal.fat}")
                            meal.fiber?.let { append("\nFiber: $it") }
                            meal.sodium?.let { append("\nSodium: $it") }
                            meal.cholesterol?.let { append("\nCholesterol: $it") }
                            append("\nTime: ${meal.timestamp.toLocalTimeString()}")
                        },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            if (adoboSnapshotForDate != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Adobo nutrition snapshot",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = buildString {
                        append("Calories: ${adoboSnapshotForDate.calories ?: "-"}")
                        append("\nProtein: ${adoboSnapshotForDate.protein ?: "-"}")
                        append("\nCarbs: ${adoboSnapshotForDate.carbs ?: "-"}")
                        append("\nFat: ${adoboSnapshotForDate.fat ?: "-"}")
                    },
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}

private fun Long.toLocalTimeString(): String {
    val time = Instant
        .fromEpochMilliseconds(this)
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .time

    val hour = time.hour.toString().padStart(2, '0')
    val minute = time.minute.toString().padStart(2, '0')
    return "$hour:$minute"
}