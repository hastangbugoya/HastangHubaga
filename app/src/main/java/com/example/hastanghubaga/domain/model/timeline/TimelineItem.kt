package com.example.hastanghubaga.domain.model.timeline

import com.example.hastanghubaga.domain.model.activity.Activity
import com.example.hastanghubaga.domain.model.meal.Meal
import com.example.hastanghubaga.domain.model.supplement.SupplementWithUserSettings
import kotlinx.datetime.LocalTime

sealed interface TimelineItem {
    val time: LocalTime

    data class SupplementTimelineItem(
        override val time: LocalTime,
        val supplement: SupplementWithUserSettings
    ) : TimelineItem

    data class ActivityTimelineItem(
        override val time: LocalTime,
        val activity: Activity
    ) : TimelineItem

    data class MealTimelineItem(
        override val time: LocalTime,
        val meal: Meal
    ) : TimelineItem
}