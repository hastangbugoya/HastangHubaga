package com.example.hastanghubaga.domain.usecase.todaytimeline

import android.util.Log
import com.example.hastanghubaga.data.local.entity.meal.AkImportedMealEntity
import com.example.hastanghubaga.data.local.mappers.toUpcomingSchedule
import com.example.hastanghubaga.domain.model.activity.Activity
import com.example.hastanghubaga.domain.model.meal.Meal
import com.example.hastanghubaga.domain.model.supplement.SupplementWithUserSettings
import com.example.hastanghubaga.domain.model.timeline.UpcomingSchedule
import com.example.hastanghubaga.domain.repository.time.UpcomingScheduleRepository
import com.example.hastanghubaga.domain.time.DomainTimePolicy
import com.example.hastanghubaga.ui.timeline.TimelineItem
import com.example.hastanghubaga.widget.snapshot.BuildWidgetDailySnapshot
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

/**
 * Builds a single, chronologically ordered timeline for "Today".
 *
 * Important:
 * - Native HH meals and AK imported meals remain separate timeline sources
 * - Imported meals are surfaced read-only
 * - No linking / merging / assignment is performed here
 */
class BuildTodayTimelineUseCase @Inject constructor(
    private val upcomingScheduleRepository: UpcomingScheduleRepository,
    private val buildWidgetDailySnapshotUseCase: BuildWidgetDailySnapshot
) {
    suspend operator fun invoke(
        supplements: List<SupplementWithUserSettings>,
        meals: List<Meal> = emptyList(),
        importedMeals: List<AkImportedMealEntity> = emptyList(),
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

        Log.d("ImportDebug", "importedMeals input size=${importedMeals.size}")
        importedMeals.forEach { importedMeal ->
            Log.d(
                "ImportDebug",
                "imported meal groupingKey=${importedMeal.groupingKey} " +
                        "logDateIso=${importedMeal.logDateIso} " +
                        "type=${importedMeal.type} " +
                        "timestamp=${importedMeal.timestamp} " +
                        "notes=${importedMeal.notes}"
            )
        }

        val importedMealItems =
            importedMeals.map { importedMeal ->
                TimelineItem.ImportedMealTimelineItem(
                    time = Instant
                        .fromEpochMilliseconds(importedMeal.timestamp)
                        .toLocalDateTime(DomainTimePolicy.localTimeZone)
                        .time,
                    meal = importedMeal,
                )
            }
        Log.d("Meow", "BuildTodayTimelineUseCase> importedMealItems: ${importedMealItems.size}")

        importedMealItems.forEach { importedMealItem ->
            Log.d(
                "ImportDebug",
                "timeline imported meal time=${importedMealItem.time} " +
                        "groupingKey=${importedMealItem.meal.groupingKey} " +
                        "type=${importedMealItem.meal.type}"
            )
        }

        val activityItems =
            activities.map { activity ->
                TimelineItem.ActivityTimelineItem(
                    time = activity.start.time,
                    activity = activity,
                )
            }
        Log.d("Meow", "BuildTodayTimelineUseCase> activityItems: ${activityItems.size}")

        val merged = (supplementItems + mealItems + importedMealItems + activityItems)
            .sortedWith(
                compareBy<TimelineItem> { it.time }
                    .thenBy {
                        when (it) {
                            is TimelineItem.SupplementTimelineItem -> 0
                            is TimelineItem.MealTimelineItem -> 1
                            is TimelineItem.ImportedMealTimelineItem -> 2
                            is TimelineItem.ActivityTimelineItem -> 3
                            is TimelineItem.SupplementDoseLogTimelineItem -> 4
                        }
                    }
                    .thenBy { it.hashCode() }
            )

        val date: LocalDate = DomainTimePolicy.todayLocal()
        val upcomingItems =
            merged.mapNotNull<TimelineItem, UpcomingSchedule> { item ->
                item.toUpcomingSchedule(date = date)
            }
        Log.d("Meow", "BuildTodayTimelineUseCase> upcomingItems: ${upcomingItems.size}")

        buildWidgetDailySnapshotUseCase(date)
        upcomingScheduleRepository.replaceAll(upcomingItems)

        return merged
    }
}