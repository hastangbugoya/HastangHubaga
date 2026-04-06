package com.example.hastanghubaga.feature.calendar.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.hastanghubaga.feature.calendar.model.DaySummaryUi
import kotlinx.datetime.*
import java.time.YearMonth

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MonthCalendarPanel(
    month: YearMonth,
    selectedDate: LocalDate?,
    summaries: Map<LocalDate, DaySummaryUi>,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onToday: () -> Unit,
    onDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    weekStartsOn: DayOfWeek = DayOfWeek.SUNDAY,
) {
    val tz = TimeZone.currentSystemDefault()
    val today = Clock.System.todayIn(tz)

    Column(modifier) {
        Header(
            month = month,
            onPrev = onPrevMonth,
            onNext = onNextMonth,
            onToday = onToday
        )

        Spacer(Modifier.height(8.dp))
        DayOfWeekRow(weekStartsOn)
        Spacer(Modifier.height(8.dp))

        val cells = buildMonthCells(month, weekStartsOn)

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxWidth(),
            userScrollEnabled = false,
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(cells) { cell ->
                val summary = summaries[cell.date]
                DayCell(
                    date = cell.date,
                    inMonth = cell.inMonth,
                    isToday = cell.date == today,
                    isSelected = selectedDate == cell.date,
                    summary = summary,
                    onClick = { onDateClick(cell.date) }
                )
            }
        }
    }
}

@Composable
private fun Header(
    month: YearMonth,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onToday: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(onClick = onPrev) { Text("‹") }

        Spacer(Modifier.weight(1f))

        Text(
            text = "${month.month.name.lowercase().replaceFirstChar { it.titlecase() }} ${month.year}",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(Modifier.weight(1f))

        TextButton(onClick = onToday) { Text("Today") }
        TextButton(onClick = onNext) { Text("›") }
    }
}

@Composable
private fun DayOfWeekRow(weekStartsOn: DayOfWeek) {
    val days = orderedDaysOfWeek(weekStartsOn)
    Row(Modifier.fillMaxWidth()) {
        days.forEach { dow ->
            Text(
                text = dow.name.take(3).lowercase().replaceFirstChar { it.titlecase() },
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    inMonth: Boolean,
    isToday: Boolean,
    isSelected: Boolean,
    summary: DaySummaryUi?,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(12.dp)

    Column(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(shape)
            .background(
                when {
                    isSelected -> MaterialTheme.colorScheme.secondaryContainer
                    isToday -> MaterialTheme.colorScheme.tertiaryContainer
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
            )
            .clickable(enabled = inMonth, onClick = onClick)
            .padding(8.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.labelLarge,
                color = if (inMonth) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
            )

            if (inMonth && summary?.hasImportedNutritionData == true) {
                ImportedDataDot()
            }
        }

        Spacer(Modifier.height(6.dp))

        if (inMonth && summary != null) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (summary.supplementsLogged > 0) MiniBadge("S ${summary.supplementsLogged}")
                if (summary.mealsLogged > 0) MiniBadge("M ${summary.mealsLogged}")
                if (summary.activitiesCompleted > 0) MiniBadge("A ${summary.activitiesCompleted}")
            }
        }
    }
}

@Composable
private fun ImportedDataDot() {
    Box(
        modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
    )
}

@Composable
private fun MiniBadge(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(text = text, style = MaterialTheme.typography.labelSmall)
    }
}

private data class MonthCell(val date: LocalDate, val inMonth: Boolean)

private fun buildMonthCells(month: YearMonth, weekStartsOn: DayOfWeek): List<MonthCell> {
    val first = LocalDate(month.year, month.month.toKotlinxMonth(), 1)
    val firstDow = first.dayOfWeek

    val offset = dayOfWeekOffset(firstDow, weekStartsOn)
    val daysInMonth = month.lengthOfMonth()

    val cells = ArrayList<MonthCell>(42)

    // leading days from previous month
    val prevMonth = month.minusMonths(1)
    val prevDays = prevMonth.lengthOfMonth()
    for (i in 0 until offset) {
        val day = prevDays - (offset - 1 - i)
        cells += MonthCell(LocalDate(prevMonth.year, prevMonth.month.toKotlinxMonth(), day), inMonth = false)
    }

    // current month
    for (d in 1..daysInMonth) {
        cells += MonthCell(LocalDate(month.year, month.month.toKotlinxMonth(), d), inMonth = true)
    }

    // trailing days to fill last row
    val nextMonth = month.plusMonths(1)
    var nextDay = 1
    while (cells.size % 7 != 0) {
        cells += MonthCell(LocalDate(nextMonth.year, nextMonth.month.toKotlinxMonth(), nextDay), inMonth = false)
        nextDay++
    }

    return cells
}

private fun orderedDaysOfWeek(weekStartsOn: DayOfWeek): List<DayOfWeek> {
    val all = DayOfWeek.entries
    val startIdx = all.indexOf(weekStartsOn)
    return (0 until 7).map { all[(startIdx + it) % 7] }
}

private fun dayOfWeekOffset(day: DayOfWeek, weekStartsOn: DayOfWeek): Int {
    val all = DayOfWeek.entries
    val dayIdx = all.indexOf(day)
    val startIdx = all.indexOf(weekStartsOn)
    return (dayIdx - startIdx + 7) % 7
}

private fun java.time.Month.toKotlinxMonth(): Month =
    Month.entries.first { it.number == this.value }
