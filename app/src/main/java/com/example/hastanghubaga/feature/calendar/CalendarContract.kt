package com.example.hastanghubaga.feature.calendar

import com.example.hastanghubaga.data.local.entity.meal.MealType
import com.example.hastanghubaga.feature.calendar.model.DaySummaryUi
import kotlinx.datetime.LocalDate
import java.time.YearMonth

object CalendarContract {

    data class State(
        val month: YearMonth,
        val selectedDate: LocalDate?,
        val summaries: Map<LocalDate, DaySummaryUi>,
        val isLoading: Boolean = false,

        // Imported Adobo snapshot (per selected day)
        val adoboSnapshot: AdoboSnapshotUi? = null,
        val savedAdoboSnapshotUri: android.net.Uri? = null,

        // HH-derived imported meals for the currently selected day
        val importedMealsForSelectedDate: List<ImportedMealUi> = emptyList()
    )

    data class AdoboSnapshotUi(
        val dateIso: String,
        val calories: Double?,
        val protein: Double?,
        val carbs: Double?,
        val fat: Double?
    )

    data class ImportedMealUi(
        val groupingKey: String,
        val type: MealType,
        val timestamp: Long,
        val notes: String?,
        val calories: Int,
        val protein: Double,
        val carbs: Double,
        val fat: Double,
        val sodium: Double? = null,
        val cholesterol: Double? = null,
        val fiber: Double? = null
    )

    sealed interface Event {
        data object PrevMonthClicked : Event
        data object NextMonthClicked : Event
        data class DateClicked(val date: LocalDate) : Event
        data object TodayClicked : Event

        data object DayPeekDismissed : Event
        data class OpenDayClicked(val date: LocalDate) : Event
    }

    sealed interface Effect {
        data class NavigateToDate(val date: LocalDate) : Effect
        data class ShowSnackbar(val message: String) : Effect

        data class OpenDayPeek(val date: LocalDate) : Effect
        data object CloseDayPeek : Effect
    }
}