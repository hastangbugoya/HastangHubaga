package com.example.hastanghubaga.ui.timeline

import com.example.hastanghubaga.data.local.entity.supplement.toDisplayCase
import com.example.hastanghubaga.domain.model.meal.MealType
import com.example.hastanghubaga.domain.model.supplement.SupplementWithUserSettings
import com.example.hastanghubaga.ui.util.UiFormatter
import com.example.hastanghubaga.ui.util.asDisplayTextNonComposable

fun SupplementWithUserSettings.toPreviewTimelineItems(): List<TimelineItem> =
    scheduledTimes.map { time ->
        TimelineItem.SupplementTimelineItem(
            time = time,
            supplement = this,
        )
    }

fun TimelineItem.toTimelineItemUiModel(): TimelineItemUiModel =
    when (this) {

        is TimelineItem.SupplementTimelineItem -> {
            SupplementUiModel(
                id = supplement.supplement.id,
                time = time,
                title = supplement.supplement.name,
                subtitle =
                    "${supplement.effectiveServingSize.asDisplayTextNonComposable()} " +
                            supplement.effectiveDoseUnit.toDisplayCase(
                                supplement.effectiveServingSize
                            ),
                isCompleted = false,
                supplementId = supplement.supplement.id,
                scheduledTime = time,
                doseState = supplement.doseState,
                defaultUnit = supplement.supplement.recommendedDoseUnit,
                suggestedDose = supplement.supplement.recommendedServingSize
            )
        }

        is TimelineItem.MealTimelineItem ->
            MealUiModel(
                id = meal.id,
                time = meal.timestamp.time,
                title = meal.type.name,
                subtitle = meal.notes,
                isCompleted = true,
                mealId = meal.id,
                mealType = meal.type.toDomain()
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
                mealType = meal.type.toDomain()
            )
        }

        is TimelineItem.ActivityTimelineItem ->
            ActivityUiModel(
                id = activity.id,
                time = activity.start.time,
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
                id = this.doseLogId,
                time = time,
                title = title,
                subtitle = buildString {
                    amount?.let { append(it) }
                    unit?.let {
                        if (isNotEmpty()) append(" ")
                        append(it)
                    }
                    scheduledTime?.let {
                        if (isNotEmpty()) append(" ")
                        append("(scheduled $it)")
                    }
                }.ifBlank { null },
                isCompleted = true,
                supplementId = supplementId,
                scheduledTime = scheduledTime,
                amountText = amount?.toString(),
                unitText = unit
            )
    }

fun List<TimelineItem>.toTimelineItemUiModels(): List<TimelineItemUiModel> =
    map { it.toTimelineItemUiModel() }

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

private fun com.example.hastanghubaga.data.local.entity.meal.MealType.toDomain(): MealType =
    when (this) {
        com.example.hastanghubaga.data.local.entity.meal.MealType.BREAKFAST ->
            MealType.BREAKFAST

        com.example.hastanghubaga.data.local.entity.meal.MealType.LUNCH ->
            MealType.LUNCH

        com.example.hastanghubaga.data.local.entity.meal.MealType.DINNER ->
            MealType.DINNER

        com.example.hastanghubaga.data.local.entity.meal.MealType.SNACK ->
            MealType.SNACK

        com.example.hastanghubaga.data.local.entity.meal.MealType.PRE_WORKOUT ->
            MealType.PRE_WORKOUT

        com.example.hastanghubaga.data.local.entity.meal.MealType.POST_WORKOUT ->
            MealType.POST_WORKOUT

        com.example.hastanghubaga.data.local.entity.meal.MealType.CUSTOM ->
            MealType.CUSTOM
    }