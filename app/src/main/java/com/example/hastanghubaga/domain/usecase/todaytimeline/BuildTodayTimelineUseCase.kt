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
import com.example.hastanghubaga.domain.usecase.meal.ResolveMealAnchorUseCase
import com.example.hastanghubaga.ui.timeline.TimelineItem
import com.example.hastanghubaga.widget.snapshot.BuildWidgetDailySnapshot
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

/**
 * Builds a single, chronologically ordered timeline for "Today".
 *
 * This use case is the central orchestration point that merges all domain
 * sources into a unified timeline representation:
 *
 * - Supplements (scheduled via recurrence / anchors upstream)
 * - Native HH meals (user-created)
 * - Imported meals (read-only from AdobongKangkong)
 * - Activities (including workouts)
 *
 * The result is:
 * - A sorted [TimelineItem] list for UI rendering
 * - A derived list of [UpcomingSchedule] persisted for widgets and background usage
 *
 * ---
 * 🧠 Responsibilities
 *
 * This use case is intentionally **composition-focused**, not logic-heavy:
 *
 * - It does NOT resolve supplement anchors (already resolved upstream)
 * - It does NOT modify timestamps for meals or activities
 * - It does NOT merge or group entities (e.g., imported meals stay independent)
 *
 * It only:
 * - Maps domain models → timeline items
 * - Sorts them deterministically
 * - Persists a simplified schedule representation
 *
 * ---
 * 🍽️ Meal Anchor Behavior
 *
 * Native HH meals may optionally expose an anchor via [ResolveMealAnchorUseCase].
 *
 * Important:
 * - The resolved anchor is **informational only** in this stage
 * - It does NOT affect:
 *   - timeline sorting
 *   - placement
 *   - scheduling
 *
 * This allows meals to later act as anchor providers (e.g., supplements anchored
 * to meals) without breaking current behavior.
 *
 * ---
 * 🏋️ Workout / Activity Behavior
 *
 * Activities are included as timeline items using their start time.
 *
 * - Activities may be marked with `isWorkout = true`
 * - This use case currently does NOT use that information
 *
 * However, this is the correct integration point for future enhancements:
 *
 * 👉 Workout-aware anchor resolution (BEFORE/DURING/AFTER_WORKOUT)
 *
 * Planned role:
 * - Extract workout activities here
 * - Provide them to anchor resolution context
 * - Keep supplements + anchor logic decoupled from Activity repository
 *
 * ---
 * 📦 Imported Meals
 *
 * Imported meals:
 * - Are treated as read-only
 * - Use their logged timestamp (converted from epoch millis)
 * - Are NOT merged with native meals
 *
 * ---
 * 📊 Sorting Rules
 *
 * Timeline items are sorted by:
 *
 * 1. Time (ascending)
 * 2. Type priority:
 *    Supplement → Meal → Imported Meal → Activity → Dose Log
 * 3. HashCode (tie-breaker for stability)
 *
 * ---
 * 🧪 Testability Notes
 *
 * This use case:
 * - Is deterministic given inputs
 * - Has no internal time mutations (uses [DomainTimePolicy.todayLocal])
 * - Keeps mapping logic simple and verifiable
 *
 * ---
 * @param supplements Supplements with resolved scheduled times
 * @param meals Native HH meals (optionally anchor-capable)
 * @param importedMeals External meals (read-only)
 * @param activities Activities for the day (may include workouts)
 *
 * @return Chronologically sorted list of [TimelineItem] for rendering
 */
class BuildTodayTimelineUseCase @Inject constructor(
    private val upcomingScheduleRepository: UpcomingScheduleRepository,
    private val buildWidgetDailySnapshotUseCase: BuildWidgetDailySnapshot,
    private val resolveMealAnchorUseCase: ResolveMealAnchorUseCase
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
                val resolvedAnchor = resolveMealAnchorUseCase(meal)

                if (resolvedAnchor != null) {
                    Log.d(
                        "MealAnchor",
                        "Meal '${meal.name}' resolved anchor=$resolvedAnchor type=${meal.type} override=${meal.treatAsAnchor}"
                    )
                }

                TimelineItem.MealTimelineItem(
                    time = meal.timestamp.time,
                    meal = meal,
                    resolvedAnchor = resolvedAnchor
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