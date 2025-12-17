package com.example.hastanghubaga.domain.usecase.todaytimeline

import com.example.hastanghubaga.domain.model.activity.Activity
import com.example.hastanghubaga.domain.model.timeline.ActivityTimelineItem
import com.example.hastanghubaga.domain.model.timeline.MealTimelineItem
import com.example.hastanghubaga.domain.model.timeline.SupplementTimelineItem
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
                    SupplementTimelineItem(
                        time = time,
                        supplement = supplementWithSettings,
                    )
                }
            }
        val mealItems =
            meals.map { meal ->
                MealTimelineItem(
                    time = meal.timestamp.toLocalTime(),
                    meal = meal,
                )
            }

        val activityItems =
            activities.map { activity ->
                ActivityTimelineItem(
                    time = activity.start.toLocalTime(),
                    activity = activity,
                )
            }

        return (supplementItems + mealItems + activityItems)
            .sortedBy { it.time }
    }
}
