package com.example.hastanghubaga.ui.timeline

import com.example.hastanghubaga.domain.model.daytimeline.ActivityTimelineItem
import com.example.hastanghubaga.domain.model.daytimeline.MealTimelineItem
import com.example.hastanghubaga.domain.model.daytimeline.SupplementTimelineItem
import com.example.hastanghubaga.domain.model.daytimeline.TodayTimelineItem
import com.example.hastanghubaga.domain.model.supplement.SupplementWithUserSettings

fun SupplementWithUserSettings.toPreviewTimelineItems(): List<TodayTimelineItem> =
    scheduledTimes.map { time ->
        SupplementTimelineItem(
            time = time,
            supplement = this,
            key = "SUPPLEMENT-${supplement.id}-$time"
        )
    }

fun TodayTimelineItem.toTimelineItemUiModel(): TimelineItemUiModel =
    when (this) {
        is SupplementTimelineItem -> {
            TimelineItemUiModel.Supplement(
                id = supplement.supplement.id,
                time = time,
                title = supplement.supplement.name,
                subtitle = "${supplement.supplement.recommendedServingSize} + ${supplement.supplement.recommendedDoseUnit}",
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

