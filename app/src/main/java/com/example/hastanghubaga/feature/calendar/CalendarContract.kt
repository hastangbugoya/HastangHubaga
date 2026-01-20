package com.example.hastanghubaga.feature.calendar

import com.example.hastanghubaga.feature.calendar.model.DaySummaryUi
import kotlinx.datetime.LocalDate
import java.time.YearMonth

object CalendarContract {

    data class State(
        val month: YearMonth,
        val selectedDate: LocalDate?,
        val summaries: Map<LocalDate, DaySummaryUi>,
        val isLoading: Boolean = false
    )

    sealed interface Event {
        data object PrevMonthClicked : Event
        data object NextMonthClicked : Event
        data class DateClicked(val date: LocalDate) : Event
        data object TodayClicked : Event

        // Day peek
        data object DayPeekDismissed : Event
        data class OpenDayClicked(val date: LocalDate) : Event
    }

    sealed interface Effect {
        data class NavigateToDate(val date: LocalDate) : Effect
        data class ShowSnackbar(val message: String) : Effect

        // Day peek
        data class OpenDayPeek(val date: LocalDate) : Effect
        data object CloseDayPeek : Effect
    }




}