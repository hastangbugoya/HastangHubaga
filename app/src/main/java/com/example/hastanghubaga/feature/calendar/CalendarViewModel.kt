package com.example.hastanghubaga.feature.calendar

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hastanghubaga.feature.calendar.CalendarContract.AdoboSnapshotUi
import com.example.hastanghubaga.feature.calendar.CalendarContract.Effect.*
import com.example.hastanghubaga.feature.calendar.model.DaySummaryUi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import org.json.JSONObject
import java.time.YearMonth

class CalendarViewModel : ViewModel() {

    private val tz = TimeZone.currentSystemDefault()

    private val _state = MutableStateFlow(
        CalendarContract.State(
            month = currentYearMonth(),
            selectedDate = Clock.System.todayIn(tz),
            summaries = emptyMap()
        )
    )
    val state: StateFlow<CalendarContract.State> = _state.asStateFlow()

    private val _effects = Channel<CalendarContract.Effect>(capacity = Channel.BUFFERED)
    val effects: Flow<CalendarContract.Effect> = _effects.receiveAsFlow()

    init {
        refreshMonthSummaries()
        Log.d("Meow", "CalendarVM init: ${hashCode()}")
    }

    override fun onCleared() {
        Log.d("Meow", "CalendarVM cleared: ${hashCode()}")
        super.onCleared()
    }

    fun onEvent(event: CalendarContract.Event) {
        when (event) {
            CalendarContract.Event.PrevMonthClicked -> {
                _state.update { it.copy(month = it.month.minusMonths(1)) }
                refreshMonthSummaries()
            }

            CalendarContract.Event.NextMonthClicked -> {
                _state.update { it.copy(month = it.month.plusMonths(1)) }
                refreshMonthSummaries()
            }

            is CalendarContract.Event.DateClicked -> {
                _state.update { it.copy(selectedDate = event.date) }
                emit(OpenDayPeek(event.date))
            }

            CalendarContract.Event.TodayClicked -> {
                val today = Clock.System.todayIn(tz)
                _state.update {
                    it.copy(
                        month = YearMonth.of(today.year, today.monthNumber()),
                        selectedDate = today
                    )
                }
                refreshMonthSummaries()
                emit(ShowSnackbar("Jumped to today"))
            }

            CalendarContract.Event.DayPeekDismissed -> {
                emit(CalendarContract.Effect.CloseDayPeek)
            }

            is CalendarContract.Event.OpenDayClicked -> {
                emit(CalendarContract.Effect.NavigateToDate(event.date))
            }
        }
    }

    private fun refreshMonthSummaries() {
        val month = _state.value.month
        val map = buildFakeSummariesForMonth(month, tz)
        _state.update { it.copy(summaries = map) }
    }

    private fun emit(effect: CalendarContract.Effect) {
        viewModelScope.launch { _effects.send(effect) }
    }

    private fun currentYearMonth(): YearMonth {
        val today = Clock.System.todayIn(tz)
        return YearMonth.of(today.year, today.monthNumber())
    }

    fun readAdoboSnapshot(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }

                val jsonString = context.contentResolver
                    .openInputStream(uri)
                    ?.bufferedReader()
                    ?.use { it.readText() }
                    ?: throw IllegalStateException("Unable to open snapshot input stream")

                val json = JSONObject(jsonString)

                val date = json.optString("dateIso")
                val macros = json.optJSONObject("macros")

                val calories = macros?.optDouble("caloriesKcal")
                val protein = macros?.optDouble("proteinG")
                val carbs = macros?.optDouble("carbsG")
                val fat = macros?.optDouble("fatG")

                Log.d("AdoboRead", "URI: $uri")
                Log.d("AdoboRead", "Date: $date")
                Log.d("AdoboRead", "Calories: $calories")
                Log.d("AdoboRead", "Protein: $protein")
                Log.d("AdoboRead", "Carbs: $carbs")
                Log.d("AdoboRead", "Fat: $fat")

                _state.update {
                    it.copy(
                        isLoading = false,
                        adoboSnapshot = AdoboSnapshotUi(
                            dateIso = date,
                            calories = calories,
                            protein = protein,
                            carbs = carbs,
                            fat = fat
                        )
                    )
                }

                emit(ShowSnackbar("Loaded Adobo snapshot for $date"))
            } catch (e: Exception) {
                Log.e("AdoboRead", "Failed to read snapshot", e)

                _state.update {
                    it.copy(
                        isLoading = false,
                        adoboSnapshot = null
                    )
                }

                emit(ShowSnackbar("Failed to read Adobo snapshot"))
            }
        }
    }
}

private fun buildFakeSummariesForMonth(
    month: YearMonth,
    tz: TimeZone
): Map<LocalDate, DaySummaryUi> {
    val start = LocalDate(month.year, month.month.toKotlinxMonth(), 1)
    val daysInMonth = month.lengthOfMonth()

    return (0 until daysInMonth).associate { offset ->
        val date = start.plus(DatePeriod(days = offset))

        val seed = (date.dayOfMonth + date.monthNumber() * 3) % 7
        val supps = if (seed % 2 == 0) seed else 0
        val meals = if (seed % 3 == 0) 1 else 0
        val acts = if (seed == 5) 1 else 0

        date to DaySummaryUi(
            date = date,
            supplementsLogged = supps,
            mealsLogged = meals,
            activitiesCompleted = acts
        )
    }
}

private fun LocalDate.monthNumber(): Int = this.month.number

private fun java.time.Month.toKotlinxMonth(): Month =
    Month.entries.first { it.number == this.value }