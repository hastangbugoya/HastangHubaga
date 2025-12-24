package com.example.hastanghubaga.domain.usecase.todaytimeline

import android.util.Log
import com.example.hastanghubaga.data.local.dao.timeline.UpcomingScheduleDao
import com.example.hastanghubaga.data.local.entity.user.UpcomingScheduleEntity
import com.example.hastanghubaga.data.local.mappers.toUpcomingSchedule
import com.example.hastanghubaga.domain.model.activity.Activity
import com.example.hastanghubaga.domain.model.timeline.TimelineItem
import com.example.hastanghubaga.domain.model.meal.Meal
import com.example.hastanghubaga.domain.model.supplement.SupplementWithUserSettings
import com.example.hastanghubaga.domain.model.timeline.UpcomingSchedule
import com.example.hastanghubaga.domain.repository.supplement.SupplementRepository
import com.example.hastanghubaga.domain.repository.time.UpcomingScheduleRepository
import com.example.hastanghubaga.domain.time.DomainTimePolicy
import com.example.hastanghubaga.ui.timeline.toTimelineItemUiModel
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

/**
 * Builds a single, chronologically ordered timeline for "Today".
 */
class BuildTodayTimelineUseCase @Inject constructor(
    private val upcomingScheduleRepository: UpcomingScheduleRepository
) {
    suspend operator fun invoke(
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
//        mealItems.forEach {
//            Log.d("Meow", "BuildTodayTimelineUseCase> mealItem: $it")
//        }
        val activityItems =
            activities.map { activity ->
                TimelineItem.ActivityTimelineItem(
                    time = activity.start.time,
                    activity = activity,
                )
            }
        Log.d("Meow", "BuildTodayTimelineUseCase> activityItems: ${activityItems.size}")
//        activityItems.forEach {
//            Log.d("Meow", "BuildTodayTimelineUseCase> activityItem: $it")
//        }
        val merged = (supplementItems + mealItems + activityItems)
            .sortedBy { it.time }

        val date: LocalDate = DomainTimePolicy.todayLocal()
        val upcomingItems =
            merged.mapNotNull<TimelineItem, UpcomingSchedule> { item ->
                item.toUpcomingSchedule(date = date)
            }
        Log.d("Meow", "BuildTodayTimelineUseCase> upcomingItems: ${upcomingItems.size}")
        upcomingScheduleRepository.replaceAll(upcomingItems)

        return merged
    }
}
