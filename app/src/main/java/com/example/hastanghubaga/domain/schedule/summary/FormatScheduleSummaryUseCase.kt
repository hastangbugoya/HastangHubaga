package com.example.hastanghubaga.domain.schedule.summary

import kotlinx.datetime.isoDayNumber
import com.example.hastanghubaga.domain.schedule.model.RecurrencePattern
import com.example.hastanghubaga.domain.schedule.model.ScheduleRule
import com.example.hastanghubaga.domain.schedule.model.ScheduleTiming
import com.example.hastanghubaga.domain.schedule.model.TimeAnchor
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime

class FormatScheduleSummaryUseCase {

    operator fun invoke(
        rule: ScheduleRule
    ): String {
        val recurrencePart = formatRecurrence(rule.recurrence)
        val timingPart = formatTiming(rule.timing)

        return buildString {
            append(recurrencePart)
            if (timingPart.isNotBlank()) {
                append(" ")
                append(timingPart)
            }
        }
    }

    private fun formatRecurrence(
        recurrence: RecurrencePattern
    ): String {
        return when (recurrence) {
            is RecurrencePattern.Daily -> {
                if (recurrence.intervalDays == 1) {
                    "Daily"
                } else {
                    "Every ${recurrence.intervalDays} days"
                }
            }

            is RecurrencePattern.Weekly -> {
                val prefix = if (recurrence.intervalWeeks == 1) {
                    "Every week"
                } else {
                    "Every ${recurrence.intervalWeeks} weeks"
                }

                val days = recurrence.daysOfWeek
                    .sortedBy { it.isoDayNumber }
                    .joinToString(", ") { it.toShortLabel() }

                "$prefix on $days"
            }
        }
    }

    private fun formatTiming(
        timing: ScheduleTiming
    ): String {
        return when (timing) {
            is ScheduleTiming.FixedTimes -> {
                val times = timing.times
                    .sortedBy { it.time }
                    .map { it.time.formatTime() }

                when (times.size) {
                    0 -> ""
                    1 -> "at ${times.first()}"
                    else -> "at " + times.dropLast(1).joinToString(", ") +
                            " and " + times.last()
                }
            }

            is ScheduleTiming.AnchoredTimes -> {
                val anchors = timing.occurrences
                    .sortedWith(
                        compareBy(
                            { it.sortOrderHint ?: Int.MAX_VALUE },
                            { it.anchor.name }
                        )
                    )
                    .map { spec ->
                        val anchorLabel = spec.anchor.toReadableLabel()

                        if (spec.offsetMinutes == 0) {
                            anchorLabel
                        } else {
                            val sign = if (spec.offsetMinutes > 0) "+" else "-"
                            val minutes = kotlin.math.abs(spec.offsetMinutes)
                            "$anchorLabel $sign ${minutes}m"
                        }
                    }

                when (anchors.size) {
                    0 -> ""
                    1 -> "at ${anchors.first()}"
                    else -> "at " + anchors.dropLast(1).joinToString(", ") +
                            " and " + anchors.last()
                }
            }
        }
    }

    private fun DayOfWeek.toShortLabel(): String {
        return when (this) {
            DayOfWeek.MONDAY -> "Mon"
            DayOfWeek.TUESDAY -> "Tue"
            DayOfWeek.WEDNESDAY -> "Wed"
            DayOfWeek.THURSDAY -> "Thu"
            DayOfWeek.FRIDAY -> "Fri"
            DayOfWeek.SATURDAY -> "Sat"
            DayOfWeek.SUNDAY -> "Sun"
        }
    }

    private fun TimeAnchor.toReadableLabel(): String {
        return when (this) {
            TimeAnchor.MIDNIGHT -> "midnight"
            TimeAnchor.WAKEUP -> "wake-up"
            TimeAnchor.BREAKFAST -> "breakfast"
            TimeAnchor.LUNCH -> "lunch"
            TimeAnchor.DINNER -> "dinner"
            TimeAnchor.BEFORE_WORKOUT -> "before workout"
            TimeAnchor.AFTER_WORKOUT -> "after workout"
            TimeAnchor.SLEEP -> "sleep"
            TimeAnchor.DURING_WORKOUT -> "during workout"
        }
    }

    private fun LocalTime.formatTime(): String {
        val hour12 = when {
            this.hour == 0 -> 12
            this.hour > 12 -> this.hour - 12
            else -> this.hour
        }

        val minutePart = this.minute.toString().padStart(2, '0')
        val period = if (this.hour < 12) "AM" else "PM"

        return "$hour12:$minutePart $period"
    }
}