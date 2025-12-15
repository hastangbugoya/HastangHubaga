package com.example.hastanghubaga.domain.model.daytimeline

import com.example.hastanghubaga.domain.model.activity.Activity
import com.example.hastanghubaga.domain.model.meal.Meal
import com.example.hastanghubaga.domain.model.supplement.SupplementWithUserSettings
import java.time.LocalTime

sealed interface TodayTimelineItem {
    val time: LocalTime
}

data class SupplementTimelineItem(
    override val time: LocalTime,
    val supplement: SupplementWithUserSettings
) : TodayTimelineItem

data class ActivityTimelineItem(
    override val time: LocalTime,
    val activity: Activity
) : TodayTimelineItem

data class MealTimelineItem(
    override val time: LocalTime,
    val meal: Meal
) : TodayTimelineItem
