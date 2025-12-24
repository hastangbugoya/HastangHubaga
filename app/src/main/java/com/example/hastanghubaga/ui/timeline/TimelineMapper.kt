package com.example.hastanghubaga.ui.timeline

import com.example.hastanghubaga.data.local.entity.supplement.toDisplayCase
import com.example.hastanghubaga.data.time.JavaTimeAdapter
import com.example.hastanghubaga.domain.model.timeline.TimelineItem
import com.example.hastanghubaga.domain.model.supplement.SupplementWithUserSettings
import com.example.hastanghubaga.domain.time.DomainTimePolicy
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
                time = meal.timestamp.time,
                title = meal.type.name,
                subtitle = meal.notes,
                type = meal.type,
            )
        is TimelineItem.ActivityTimelineItem ->
            TimelineItemUiModel.Activity(
                id = activity.id,
                time = activity.start.time,
                title = activity.type.name,
                subtitle = UiFormatter.formatTimeRange(start = activity.start, end = activity.end),
                activityType = activity.type,
                endTime = activity.end?.time
            )
    }

fun List<TimelineItem>.toTimelineItemUiModels(): List<TimelineItemUiModel> =
    map { it.toTimelineItemUiModel() }
