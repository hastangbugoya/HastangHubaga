package com.example.hastanghubaga.ui.timeline

import com.example.hastanghubaga.data.local.entity.supplement.toDisplayCase
import com.example.hastanghubaga.domain.model.timeline.TimelineItem
import com.example.hastanghubaga.domain.model.supplement.SupplementWithUserSettings
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
            TimelineItemUiModel.Supplement(
                id = supplement.supplement.id,
                time = time,
                title = supplement.supplement.name,
                subtitle = "${supplement.effectiveServingSize.asDisplayTextNonComposable()} ${
                    supplement.effectiveDoseUnit.toDisplayCase(
                        supplement.effectiveServingSize
                    )
                }",
                doseState = supplement.doseState,
                suggestedDose = supplement.supplement.recommendedServingSize,
                defaultUnit =supplement.supplement.recommendedDoseUnit,
            )
        }
        is TimelineItem.MealTimelineItem ->
            TimelineItemUiModel.Meal(
                id = meal.id,
                time = meal.timestamp.toLocalTime(),
                title = meal.type.name,
                subtitle = meal.notes,
                type = meal.type,
            )
        is TimelineItem.ActivityTimelineItem ->
            TimelineItemUiModel.Activity(
                id = activity.id,
                time = activity.start.toLocalTime(),
                title = activity.type.name,
                subtitle = "${activity.start.toLocalTime()}${activity.end?.let { " to ${it.toLocalTime()}" }}",
                activityType = activity.type,
                endTime = activity.end?.toLocalTime()
            )
    }

fun List<TimelineItem>.toTimelineItemUiModels(): List<TimelineItemUiModel> =
    map { it.toTimelineItemUiModel() }
