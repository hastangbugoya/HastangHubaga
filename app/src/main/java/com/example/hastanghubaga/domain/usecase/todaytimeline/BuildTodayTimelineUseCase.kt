package com.example.hastanghubaga.domain.usecase.todaytimeline

import android.util.Log
import com.example.hastanghubaga.domain.model.activity.Activity
import com.example.hastanghubaga.domain.model.timeline.TimelineItem
import com.example.hastanghubaga.domain.model.meal.Meal
import com.example.hastanghubaga.domain.model.supplement.SupplementWithUserSettings
import javax.inject.Inject

/**
 * Builds a single, chronologically ordered timeline for "Today".
 */
class BuildTodayTimelineUseCase @Inject constructor() {
    operator fun invoke(
        supplements: List<SupplementWithUserSettings>,
        meals: List<Meal> = emptyList(),
        activities: List<Activity> = emptyList()
    ): List<TimelineItem> {
        val supplementItems =
            supplements.flatMap { supplementWithSettings ->
                supplementWithSettings.scheduledTimes.map { time ->
                    TimelineItem.SupplementTimelineItem(
                        time = time,
                        supplement = supplementWithSettings,
                    )
                }
            }
        Log.d("Meow", "BuildTodayTimelineUseCase> supplementItems: ${supplementItems.size}")
        val mealItems =
            meals.map { meal ->
                TimelineItem.MealTimelineItem(
                    time = meal.timestamp.time,
                    meal = meal,
                )
            }
        Log.d("Meow", "BuildTodayTimelineUseCase> mealItems: ${mealItems.size}")
        mealItems.forEach {
            Log.d("Meow", "BuildTodayTimelineUseCase> mealItem: $it")
        }
        val activityItems =
            activities.map { activity ->
                TimelineItem.ActivityTimelineItem(
                    time = activity.start.time,
                    activity = activity,
                )
            }
        Log.d("Meow", "BuildTodayTimelineUseCase> activityItems: ${activityItems.size}")
        activityItems.forEach {
            Log.d("Meow", "BuildTodayTimelineUseCase> activityItem: $it")
        }
        return (supplementItems + mealItems + activityItems)
            .sortedBy { it.time }
    }
}
