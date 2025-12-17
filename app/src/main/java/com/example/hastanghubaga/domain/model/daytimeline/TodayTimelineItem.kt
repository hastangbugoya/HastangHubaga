package com.example.hastanghubaga.domain.model.daytimeline

import com.example.hastanghubaga.domain.model.activity.Activity
import com.example.hastanghubaga.domain.model.meal.Meal
import com.example.hastanghubaga.domain.model.supplement.SupplementWithUserSettings
import java.time.LocalTime

sealed interface TodayTimelineItem {
    val time: LocalTime
}

data class SupplementTimelineItem(
    val key: String,
    override val time: LocalTime,
    val supplement: SupplementWithUserSettings
) : TodayTimelineItem

data class ActivityTimelineItem(
    val key: String,
    override val time: LocalTime,
    val activity: Activity
) : TodayTimelineItem

data class MealTimelineItem(
    val key: String,
    override val time: LocalTime,
    val meal: Meal
) : TodayTimelineItem
