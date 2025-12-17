package com.example.hastanghubaga.domain.usecase.todaytimeline

import com.example.hastanghubaga.domain.model.activity.Activity
import com.example.hastanghubaga.domain.model.daytimeline.ActivityTimelineItem
import com.example.hastanghubaga.domain.model.daytimeline.MealTimelineItem
import com.example.hastanghubaga.domain.model.daytimeline.SupplementTimelineItem
import com.example.hastanghubaga.domain.model.daytimeline.TodayTimelineItem
import com.example.hastanghubaga.domain.model.meal.Meal
import com.example.hastanghubaga.domain.model.supplement.SupplementWithUserSettings
import java.time.LocalTime
import javax.inject.Inject

/**
 * Builds a single, chronologically ordered timeline for "Today".
 *
 * IMPORTANT:
 * - Produces ONE timeline item per scheduled supplement dose
 * - Keys are stable: TYPE + ENTITY_ID + TIME
 * - Sorting is centralized here (domain responsibility)
 */
class BuildTodayTimelineUseCase @Inject constructor() {

    operator fun invoke(
        supplements: List<SupplementWithUserSettings>,
        meals: List<Meal> = emptyList(),
        activities: List<Activity> = emptyList()
    ): List<TodayTimelineItem> {
        val supplementItems =
            supplements.flatMap { supplementWithSettings ->
                supplementWithSettings.scheduledTimes.map { time ->
                    SupplementTimelineItem(
                        time = time,
                        supplement = supplementWithSettings,
                        key = supplementKey(
                            supplementId = supplementWithSettings.supplement.id,
                            time = time
                        )
                    )
                }
            }
        val mealItems =
            meals.map { meal ->
                MealTimelineItem(
                    time = meal.timestamp.toLocalTime(),
                    meal = meal,
                    key = mealKey(
                        mealId = meal.id,
                        time = meal.timestamp.toLocalTime()
                    )
                )
            }

        val activityItems =
            activities.map { activity ->
                ActivityTimelineItem(
                    time = activity.start.toLocalTime(),
                    activity = activity,
                    key = activityKey(
                        activityId = activity.id,
                        time = activity.start.toLocalTime()
                    )
                )
            }

        return (supplementItems + mealItems + activityItems)
            .sortedBy { it.time }
    }

    // ---------- Key builders ----------

    private fun supplementKey(
        supplementId: Long,
        time: LocalTime
    ): String =
        "SUPPLEMENT-$supplementId-$time"

    private fun mealKey(
        mealId: Long,
        time: LocalTime
    ): String =
        "MEAL-$mealId-$time"

    private fun activityKey(
        activityId: Long,
        time: LocalTime
    ): String =
        "ACTIVITY-$activityId-$time"
}
