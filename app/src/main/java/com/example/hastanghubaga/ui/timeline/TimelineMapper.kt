package com.example.hastanghubaga.ui.timeline

import android.util.Log
import com.example.hastanghubaga.data.local.entity.supplement.toDisplayCase
import com.example.hastanghubaga.domain.model.activity.ActivityType
import com.example.hastanghubaga.domain.model.supplement.SupplementWithUserSettings
import com.example.hastanghubaga.ui.util.asDisplayTextNonComposable

fun SupplementWithUserSettings.toPreviewTimelineItems(): List<TimelineItem> {
    val subtitle =
        "${effectiveServingSize.asDisplayTextNonComposable()} " +
                effectiveDoseUnit.toDisplayCase(effectiveServingSize)

    val resolvedEntries = resolvedScheduleEntries

    return if (resolvedEntries.isNotEmpty()) {
        resolvedEntries.map { entry ->
            TimelineItem.SupplementTimelineItem(
                time = entry.time,
                occurrenceId = entry.occurrenceId ?: buildPreviewOccurrenceId(
                    supplementId = supplement.id,
                    plannedTimeSeconds = entry.time.toSecondOfDay(),
                    scheduleId = entry.scheduleId,
                    sourceRowId = entry.sourceRowId,
                    sortOrder = entry.sortOrder
                ),
                supplementId = supplement.id,
                title = supplement.name,
                subtitle = subtitle,
                defaultUnit = supplement.recommendedDoseUnit,
                suggestedDose = supplement.recommendedServingSize,
                doseState = doseState,
                scheduledTime = entry.time,
                isTaken = false
            )
        }
    } else {
        scheduledTimes.mapIndexed { index, time ->
            TimelineItem.SupplementTimelineItem(
                time = time,
                occurrenceId = buildPreviewOccurrenceId(
                    supplementId = supplement.id,
                    plannedTimeSeconds = time.toSecondOfDay(),
                    scheduleId = null,
                    sourceRowId = null,
                    sortOrder = index
                ),
                supplementId = supplement.id,
                title = supplement.name,
                subtitle = subtitle,
                defaultUnit = supplement.recommendedDoseUnit,
                suggestedDose = supplement.recommendedServingSize,
                doseState = doseState,
                scheduledTime = time,
                isTaken = false
            )
        }
    }
}

fun TimelineItem.toTimelineItemUiModel(): TimelineItemUiModel =
    when (this) {
        is TimelineItem.SupplementTimelineItem ->
            SupplementUiModel(
                id = supplementId,
                time = time,
                title = title,
                subtitle = subtitle,
                isCompleted = isTaken,
                supplementId = supplementId,
                scheduledTime = scheduledTime,
                doseState = doseState,
                defaultUnit = defaultUnit,
                suggestedDose = suggestedDose,
                occurrenceId = occurrenceId,
                ingredients = ingredients
            )

        is TimelineItem.MealTimelineItem -> {
            Log.d(
                "MEAL_RECON",
                "map MealTimelineItem -> MealUiModel mealId=${meal.id} type=${meal.type} occurrenceId=$occurrenceId isCompleted=$isCompleted time=$time"
            )

            MealUiModel(
                id = meal.id,
                time = time,
                title = meal.name
                    .takeIf { it.isNotBlank() }
                    ?: meal.type.name,
                subtitle = meal.notes,
                isCompleted = isCompleted,
                mealId = meal.id,
                mealType = meal.type,
                occurrenceId = occurrenceId
            )
        }

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
                id = activityId,
                time = time,
                title = title,
                subtitle = subtitle ?: buildActivitySubtitle(
                    scheduledTime = scheduledTime,
                    isWorkout = isWorkout
                ),
                isCompleted = isCompleted,
                activityId = activityId,
                activityType = title.toActivityTypeOrOther(),
                startTime = time,
                endTime = null,
                intensity = null,
                occurrenceId = occurrenceId
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
                unitText = unit,
                ingredients = ingredients
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

private fun buildActivitySubtitle(
    scheduledTime: kotlinx.datetime.LocalTime,
    isWorkout: Boolean
): String {
    return if (isWorkout) {
        "Scheduled $scheduledTime • Workout"
    } else {
        "Scheduled $scheduledTime"
    }
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

private fun buildPreviewOccurrenceId(
    supplementId: Long,
    plannedTimeSeconds: Int,
    scheduleId: Long?,
    sourceRowId: Long?,
    sortOrder: Int
): String {
    return listOf(
        "preview",
        supplementId.toString(),
        scheduleId?.toString() ?: "ns",
        sourceRowId?.toString() ?: "nr",
        plannedTimeSeconds.toString(),
        sortOrder.toString()
    ).joinToString(separator = "|")
}

private fun String.toActivityTypeOrOther(): ActivityType =
    runCatching {
        ActivityType.valueOf(
            uppercase().replace(" ", "_")
        )
    }.getOrDefault(ActivityType.OTHER)