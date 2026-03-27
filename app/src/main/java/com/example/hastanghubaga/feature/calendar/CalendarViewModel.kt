package com.example.hastanghubaga.feature.calendar

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
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
import org.json.JSONArray
import org.json.JSONObject
import java.time.YearMonth

private const val PREFS_NAME = "adobo_snapshot_prefs"
private const val KEY_ADOBO_SNAPSHOT_URI = "adobo_snapshot_uri"

class CalendarViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val appContext: Context = application.applicationContext
    private val tz = TimeZone.currentSystemDefault()

    private val _state = MutableStateFlow(
        CalendarContract.State(
            month = currentYearMonth(),
            selectedDate = Clock.System.todayIn(tz),
            summaries = emptyMap(),
            savedAdoboSnapshotUri = loadSavedAdoboSnapshotUri()
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
                readAdoboSnapshot(buildSnapshotUriForDate(event.date))
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

    fun readSavedAdoboSnapshotIfAvailable() {
        val selectedDate = _state.value.selectedDate ?: Clock.System.todayIn(tz)
        val uri = buildSnapshotUriForDate(selectedDate)

        Log.d("AdoboRead", "Reading snapshot for date=$selectedDate uri=$uri")

        readAdoboSnapshot(uri)
    }

    private fun buildSnapshotUriForDate(date: LocalDate): Uri {
        return Uri.parse(
            "content://com.example.adobongkangkong.shared/snapshot/$date"
        )
    }

    private fun buildMonthSnapshotUri(month: YearMonth): Uri {
        return Uri.parse(
            "content://com.example.adobongkangkong.shared/snapshot-month/$month"
        )
    }

    private fun refreshMonthSummaries() {
        val month = _state.value.month
        val map = buildFakeSummariesForMonth(month, tz)
        _state.update { current ->
            current.copy(
                summaries = map
            )
        }

        reapplyAdoboSnapshotToSummaries()
        readAdoboMonthSnapshot(month)
    }

    private fun emit(effect: CalendarContract.Effect) {
        viewModelScope.launch { _effects.send(effect) }
    }

    private fun currentYearMonth(): YearMonth {
        val today = Clock.System.todayIn(tz)
        return YearMonth.of(today.year, today.monthNumber())
    }

    private fun readAdoboSnapshot(uri: Uri) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }

                val jsonString = appContext.contentResolver
                    .openInputStream(uri)
                    ?.bufferedReader()
                    ?.use { it.readText() }
                    ?: throw IllegalStateException("Unable to open snapshot input stream")

                val json = JSONObject(jsonString)

                val dateIso = json.optString("dateIso")
                val macros = json.optJSONObject("macros")

                val calories = macros?.optDouble("caloriesKcal")
                val protein = macros?.optDouble("proteinG")
                val carbs = macros?.optDouble("carbsG")
                val fat = macros?.optDouble("fatG")

                Log.d("AdoboRead", "URI: $uri")
                Log.d("AdoboRead", "Date: $dateIso")
                Log.d("AdoboRead", "Calories: $calories")
                Log.d("AdoboRead", "Protein: $protein")
                Log.d("AdoboRead", "Carbs: $carbs")
                Log.d("AdoboRead", "Fat: $fat")

                val snapshot = AdoboSnapshotUi(
                    dateIso = dateIso,
                    calories = calories,
                    protein = protein,
                    carbs = carbs,
                    fat = fat
                )

                _state.update {
                    it.copy(
                        isLoading = false,
                        adoboSnapshot = snapshot
                    )
                }

                applyAdoboSnapshotToSummaries(snapshot)
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

    private fun readAdoboMonthSnapshot(month: YearMonth) {
        val uri = buildMonthSnapshotUri(month)

        Log.d("AdoboRead", "Reading month snapshot uri=$uri")

        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }

                val jsonString = appContext.contentResolver
                    .openInputStream(uri)
                    ?.bufferedReader()
                    ?.use { it.readText() }
                    ?: throw IllegalStateException("Unable to open month snapshot input stream")

                val json = JSONObject(jsonString)
                val days = json.optJSONArray("days")
                    ?: throw IllegalStateException("Month snapshot missing days array")

                Log.d("AdoboRead", "Month URI: $uri")
                Log.d("AdoboRead", "Month days count: ${days.length()}")

                applyAdoboMonthSnapshotToSummaries(days)

                _state.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                Log.e("AdoboRead", "Failed to read month snapshot", e)

                _state.update { it.copy(isLoading = false) }

                emit(ShowSnackbar("Failed to read Adobo month snapshot"))
            }
        }
    }

    private fun applyAdoboMonthSnapshotToSummaries(days: JSONArray) {
        val updates = mutableMapOf<LocalDate, DaySummaryUi>()

        for (i in 0 until days.length()) {
            val day = days.optJSONObject(i) ?: continue
            val dateIso = day.optString("dateIso")
            val snapshotDate = parseIsoDateOrNull(dateIso) ?: continue

            val existing = _state.value.summaries[snapshotDate]

            updates[snapshotDate] = DaySummaryUi(
                date = snapshotDate,
                supplementsLogged = existing?.supplementsLogged ?: 0,
                mealsLogged = 1,
                activitiesCompleted = existing?.activitiesCompleted ?: 0,
                hasImportedNutritionData = true
            )
        }

        if (updates.isEmpty()) return

        _state.update { current ->
            current.copy(
                summaries = current.summaries + updates
            )
        }
    }

    private fun applyAdoboSnapshotToSummaries(snapshot: AdoboSnapshotUi) {
        val snapshotDate = parseIsoDateOrNull(snapshot.dateIso) ?: return

        _state.update { current ->
            val existing = current.summaries[snapshotDate]

            val updatedSummary = DaySummaryUi(
                date = snapshotDate,
                supplementsLogged = existing?.supplementsLogged ?: 0,
                mealsLogged = 1,
                activitiesCompleted = existing?.activitiesCompleted ?: 0,
                hasImportedNutritionData = true
            )

            current.copy(
                summaries = current.summaries + (snapshotDate to updatedSummary)
            )
        }
    }

    private fun reapplyAdoboSnapshotToSummaries() {
        val snapshot = _state.value.adoboSnapshot ?: return
        applyAdoboSnapshotToSummaries(snapshot)
    }

    private fun parseIsoDateOrNull(raw: String): LocalDate? {
        return try {
            LocalDate.parse(raw)
        } catch (_: Exception) {
            null
        }
    }

    private fun saveAdoboSnapshotUri(uri: Uri) {
        appContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_ADOBO_SNAPSHOT_URI, uri.toString())
            .apply()
    }

    private fun loadSavedAdoboSnapshotUri(): Uri? {
        val raw = appContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_ADOBO_SNAPSHOT_URI, null)

        return raw?.let(Uri::parse)
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