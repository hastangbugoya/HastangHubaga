package com.example.hastanghubaga.ui.timeline

import com.example.hastanghubaga.data.local.entity.supplement.toDisplayCase
import com.example.hastanghubaga.domain.model.timeline.ActivityTimelineItem
import com.example.hastanghubaga.domain.model.timeline.MealTimelineItem
import com.example.hastanghubaga.domain.model.timeline.SupplementTimelineItem
import com.example.hastanghubaga.domain.model.timeline.TimelineItem
import com.example.hastanghubaga.domain.model.supplement.SupplementWithUserSettings
import com.example.hastanghubaga.ui.util.asDisplayTextNonComposable

fun SupplementWithUserSettings.toPreviewTimelineItems(): List<TimelineItem> =
    scheduledTimes.map { time ->
        SupplementTimelineItem(
            time = time,
            supplement = this,
        )
    }

fun TimelineItem.toTimelineItemUiModel(): TimelineItemUiModel =
    when (this) {
        is SupplementTimelineItem -> {
            TimelineItemUiModel.Supplement(
                id = supplement.supplement.id,
                time = time,
                title = supplement.supplement.name,
                subtitle = "${supplement.effectiveServingSize.asDisplayTextNonComposable()} ${supplement.effectiveDoseUnit.toDisplayCase(supplement.effectiveServingSize)}",
                doseState = supplement.doseState,
            )
        }
        is MealTimelineItem ->
            TimelineItemUiModel.Meal(
                id = meal.id,
                time = meal.timestamp.toLocalTime(),
                title = meal.type.name,
                subtitle = meal.notes,
                type = meal.type,
            )
        is ActivityTimelineItem ->
            TimelineItemUiModel.Activity(
                id = activity.id,
                time = activity.start.toLocalTime(),
                title = activity.type.name,
                subtitle = "${activity.start.toLocalTime()}${activity.end?.let { " to ${it.toLocalTime()}" }}"
            )
    }

