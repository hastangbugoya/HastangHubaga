package com.example.hastanghubaga.feature.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hastanghubaga.feature.calendar.CalendarContract.Effect.*
import com.example.hastanghubaga.feature.calendar.model.DaySummaryUi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.*
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
        // seed fake summaries for initial month
        refreshMonthSummaries()
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
                emit(CalendarContract.Effect.OpenDayPeek(event.date))
            }

            CalendarContract.Event.TodayClicked -> {
                val today = Clock.System.todayIn(tz)
                _state.update { it.copy(month = YearMonth.of(today.year, today.monthNumber()), selectedDate = today) }
                refreshMonthSummaries()
                emit(ShowSnackbar("Jumped to today"))
            }

            CalendarContract.Event.DayPeekDismissed -> {
                emit(CalendarContract.Effect.CloseDayPeek) // ✅ let screen close it
            }

            is CalendarContract.Event.OpenDayClicked ->  {
                emit(CalendarContract.Effect.NavigateToDate(event.date)) // ✅ sheet button triggers nav
            }
        }
    }

    private fun refreshMonthSummaries() {
        val month = _state.value.month

        // Fake deterministic-ish data so it “feels real” but doesn’t require DB yet.
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
}

private fun buildFakeSummariesForMonth(
    month: YearMonth,
    tz: TimeZone
): Map<LocalDate, DaySummaryUi> {
    val start = LocalDate(month.year, month.month.toKotlinxMonth(), 1)
    val daysInMonth = month.lengthOfMonth()

    return (0 until daysInMonth).associate { offset ->
        val date = start.plus(DatePeriod(days = offset))

        // deterministic fake “counts”
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
