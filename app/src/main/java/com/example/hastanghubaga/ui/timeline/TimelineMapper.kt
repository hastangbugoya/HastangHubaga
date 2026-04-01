package com.example.hastanghubaga.ui.timeline

import com.example.hastanghubaga.data.local.entity.supplement.toDisplayCase
import com.example.hastanghubaga.domain.model.supplement.SupplementWithUserSettings
import com.example.hastanghubaga.ui.util.UiFormatter
import com.example.hastanghubaga.ui.util.asDisplayTextNonComposable

fun SupplementWithUserSettings.toPreviewTimelineItems(): List<TimelineItem> {
    val resolvedEntries = resolvedScheduleEntries

    return if (resolvedEntries.isNotEmpty()) {
        resolvedEntries.map { entry ->
            TimelineItem.SupplementTimelineItem(
                time = entry.time,
                supplement = this,
                resolvedScheduleEntry = entry
            )
        }
    } else {
        scheduledTimes.map { time ->
            TimelineItem.SupplementTimelineItem(
                time = time,
                supplement = this,
                resolvedScheduleEntry = null
            )
        }
    }
}

fun TimelineItem.toTimelineItemUiModel(): TimelineItemUiModel =
    when (this) {
        is TimelineItem.SupplementTimelineItem -> {
            val plannedTime =
                resolvedScheduleEntry?.time ?: time

            SupplementUiModel(
                id = supplement.supplement.id,
                time = time,
                title = supplement.supplement.name,
                subtitle =
                    "${supplement.effectiveServingSize.asDisplayTextNonComposable()} " +
                            supplement.effectiveDoseUnit.toDisplayCase(
                                supplement.effectiveServingSize
                            ),
                isCompleted = isTaken,
                supplementId = supplement.supplement.id,
                scheduledTime = plannedTime,
                doseState = supplement.doseState,
                defaultUnit = supplement.supplement.recommendedDoseUnit,
                suggestedDose = supplement.supplement.recommendedServingSize,
                occurrenceId = occurrenceId
            )
        }

        is TimelineItem.MealTimelineItem ->
            MealUiModel(
                id = meal.id,
                time = time,
                title = meal.name
                    .takeIf { it.isNotBlank() }
                    ?: meal.type.name,
                subtitle = meal.notes,
                isCompleted = true,
                mealId = meal.id,
                mealType = meal.type
            )

        is TimelineItem.ImportedMealTimelineItem -> {
            val importedMealId = importedMealStableId(meal.groupingKey)

            ImportedMealUiModel(
                id = importedMealId,
                time = time,
                title = meal.type.name,
                subtitle = buildImportedMealSubtitle(meal.notes),
                isCompleted = true,
                importedMealId = importedMealId,
                mealType = meal.type
            )
        }

        is TimelineItem.ActivityTimelineItem ->
            ActivityUiModel(
                id = activity.id,
                time = time,
                title = activity.type.name,
                subtitle = UiFormatter.formatTimeRange(
                    start = activity.start,
                    end = activity.end
                ),
                isCompleted = activity.end != null,
                activityId = activity.id,
                activityType = activity.type,
                startTime = activity.start.time,
                endTime = activity.end?.time,
                intensity = activity.intensity
            )

        is TimelineItem.SupplementDoseLogTimelineItem ->
            SupplementDoseLogUiModel(
                id = doseLogId,
                time = time,
                title = title,
                subtitle = buildDoseLogSubtitle(
                    amount = amount,
                    unit = unit,
                    scheduledTime = scheduledTime
                ),
                isCompleted = true,
                supplementId = supplementId,
                scheduledTime = scheduledTime,
                amountText = amount?.toString(),
                unitText = unit
            )
    }

fun List<TimelineItem>.toTimelineItemUiModels(): List<TimelineItemUiModel> =
    map { it.toTimelineItemUiModel() }

private fun buildDoseLogSubtitle(
    amount: Double?,
    unit: String?,
    scheduledTime: kotlinx.datetime.LocalTime?
): String? {
    return buildString {
        amount?.let { append(it) }
        unit?.let {
            if (isNotEmpty()) append(" ")
            append(it)
        }
        scheduledTime?.let {
            if (isNotEmpty()) append(" ")
            append("(scheduled $it)")
        }
    }.ifBlank { null }
}

private fun buildImportedMealSubtitle(notes: String?): String {
    val base = notes?.takeIf { it.isNotBlank() }
    return if (base != null) {
        "$base • Imported"
    } else {
        "Imported"
    }
}

private fun importedMealStableId(groupingKey: String): Long {
    val positive = groupingKey.hashCode().toLong() and 0x7fffffffL
    return -positive.coerceAtLeast(1L)
}