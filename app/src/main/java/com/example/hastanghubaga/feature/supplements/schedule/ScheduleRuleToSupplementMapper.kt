package com.example.hastanghubaga.feature.supplements.schedule

import com.example.hastanghubaga.data.local.entity.supplement.DoseAnchorType
import com.example.hastanghubaga.data.local.entity.supplement.FrequencyType
import com.example.hastanghubaga.domain.schedule.model.RecurrencePattern
import com.example.hastanghubaga.domain.schedule.model.ScheduleRule
import com.example.hastanghubaga.domain.schedule.model.ScheduleTiming
import com.example.hastanghubaga.feature.schedule.ui.model.AnchorTypeUi
import com.example.hastanghubaga.feature.schedule.ui.model.ParsedScheduleEditorDraft
import kotlinx.datetime.toJavaLocalDate

/**
 * Converts parsed schedule editor draft → SupplementEntity fields.
 *
 * This is a TEMPORARY adapter until supplements fully adopt ScheduleRule.
 */
object ScheduleRuleToSupplementMapper {

    fun mapToEntityFields(
        draft: ParsedScheduleEditorDraft
    ): SupplementScheduleFields {

        val (frequencyType, frequencyInterval, weeklyDays) =
            mapRecurrence(draft)

        val (anchorType, offsetMinutes) =
            mapTiming(draft)

        val startDate = draft.startDate.toJavaLocalDate().toString()

        return SupplementScheduleFields(
            frequencyType = frequencyType,
            frequencyInterval = frequencyInterval,
            weeklyDays = weeklyDays,
            doseAnchorType = anchorType,
            offsetMinutes = offsetMinutes,
            startDate = startDate
        )
    }

    private fun mapRecurrence(
        draft: ParsedScheduleEditorDraft
    ): Triple<FrequencyType, Int?, List<java.time.DayOfWeek>?> {

        return when (draft.recurrenceMode) {
            com.example.hastanghubaga.feature.schedule.ui.model.RecurrenceMode.DAILY -> {
                if (draft.interval == 1) {
                    Triple(FrequencyType.DAILY, null, null)
                } else {
                    Triple(FrequencyType.EVERY_X_DAYS, draft.interval, null)
                }
            }

            com.example.hastanghubaga.feature.schedule.ui.model.RecurrenceMode.WEEKLY -> {
                val days = draft.selectedWeekdays.map {
                    mapWeekday(it)
                }

                Triple(
                    FrequencyType.WEEKLY,
                    draft.interval,
                    days
                )
            }
        }
    }

    private fun mapTiming(
        draft: ParsedScheduleEditorDraft
    ): Pair<DoseAnchorType, Int?> {

        return when (draft.timingMode) {
            com.example.hastanghubaga.feature.schedule.ui.model.TimingMode.FIXED -> {
                // FIXED times do not map cleanly to current schema.
                // Use MIDNIGHT anchor as fallback.
                DoseAnchorType.MIDNIGHT to 0
            }

            com.example.hastanghubaga.feature.schedule.ui.model.TimingMode.ANCHORED -> {
                val first = draft.anchoredTimes.firstOrNull()

                val anchor = mapAnchor(first?.anchor)
                val offset = first?.offsetMinutes ?: 0

                anchor to offset
            }
        }
    }

    private fun mapAnchor(anchor: AnchorTypeUi?): DoseAnchorType {
        return when (anchor) {
            AnchorTypeUi.WAKE_UP -> DoseAnchorType.WAKEUP
            AnchorTypeUi.BREAKFAST -> DoseAnchorType.BREAKFAST
            AnchorTypeUi.LUNCH -> DoseAnchorType.LUNCH
            AnchorTypeUi.DINNER -> DoseAnchorType.DINNER
            AnchorTypeUi.SLEEP -> DoseAnchorType.SLEEP
            null -> DoseAnchorType.MIDNIGHT
        }
    }

    private fun mapWeekday(
        weekday: com.example.hastanghubaga.feature.schedule.ui.model.WeekdayUi
    ): java.time.DayOfWeek {
        return java.time.DayOfWeek.valueOf(weekday.name)
    }
}

data class SupplementScheduleFields(
    val frequencyType: FrequencyType,
    val frequencyInterval: Int?,
    val weeklyDays: List<java.time.DayOfWeek>?,
    val doseAnchorType: DoseAnchorType,
    val offsetMinutes: Int?,
    val startDate: String?
)