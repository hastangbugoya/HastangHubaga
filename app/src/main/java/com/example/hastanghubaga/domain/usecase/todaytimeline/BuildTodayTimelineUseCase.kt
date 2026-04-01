package com.example.hastanghubaga.domain.usecase.todaytimeline

import android.util.Log
import com.example.hastanghubaga.data.local.entity.meal.AkImportedMealEntity
import com.example.hastanghubaga.data.local.mappers.toUpcomingSchedule
import com.example.hastanghubaga.domain.model.activity.Activity
import com.example.hastanghubaga.domain.model.meal.Meal
import com.example.hastanghubaga.domain.model.supplement.SupplementDoseLog
import com.example.hastanghubaga.domain.model.supplement.SupplementWithUserSettings
import com.example.hastanghubaga.domain.model.timeline.UpcomingSchedule
import com.example.hastanghubaga.domain.repository.time.UpcomingScheduleRepository
import com.example.hastanghubaga.domain.time.DomainTimePolicy
import com.example.hastanghubaga.domain.usecase.meal.ResolveMealAnchorUseCase
import com.example.hastanghubaga.ui.timeline.TimelineItem
import com.example.hastanghubaga.widget.snapshot.BuildWidgetDailySnapshot
import javax.inject.Inject
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toLocalDateTime

/**
 * Builds a single, chronologically ordered timeline for "Today".
 *
 * This use case is the central orchestration point that merges multiple
 * time-resolved item types into one user-facing day timeline:
 *
 * - Supplements (already scheduled/resolved upstream)
 * - Actual supplement dose logs
 * - Native HH meals
 * - Imported meals
 * - Activities
 *
 * Canonical timeline rule:
 * - This use case trusts each item's already-resolved [TimelineItem.time]
 * - It does NOT perform scheduling or anchor resolution for supplements
 * - It does NOT reinterpret source timestamps once a [TimelineItem] is built
 *
 * Its job is to make the mixed timeline make user-sense by:
 * - mapping source models to timeline rows
 * - merging heterogeneous rows
 * - applying deterministic cross-type ordering
 * - persisting a simplified upcoming schedule snapshot
 *
 * Same-time ordering policy:
 * - Supplements first
 * - Native meals second
 * - Imported meals third
 * - Activities fourth
 * - Supplement dose logs fifth
 *
 * Within the same type and time, ordering must be stable and identity-based.
 * Hash-based ordering is intentionally avoided because it is not a business rule.
 */
class BuildTodayTimelineUseCase @Inject constructor(
    private val upcomingScheduleRepository: UpcomingScheduleRepository,
    private val buildWidgetDailySnapshotUseCase: BuildWidgetDailySnapshot,
    private val resolveMealAnchorUseCase: ResolveMealAnchorUseCase
) {
    suspend operator fun invoke(
        supplements: List<SupplementWithUserSettings>,
        supplementDoseLogs: List<SupplementDoseLog> = emptyList(),
        meals: List<Meal> = emptyList(),
        importedMeals: List<AkImportedMealEntity> = emptyList(),
        activities: List<Activity> = emptyList()
    ): List<TimelineItem> {
        val supplementTitleLookup = supplements.associate { item ->
            item.supplement.id to item.supplement.name
        }

        val supplementItems =
            supplements.flatMap { supplementWithSettings ->
                val resolvedEntries = supplementWithSettings.resolvedScheduleEntries

                if (resolvedEntries.isNotEmpty()) {
                    resolvedEntries.map { entry ->
                        TimelineItem.SupplementTimelineItem(
                            time = entry.time,
                            supplement = supplementWithSettings,
                            resolvedScheduleEntry = entry,
                            occurrenceId = entry.occurrenceId
                        )
                    }
                } else {
                    supplementWithSettings.scheduledTimes.map { time ->
                        TimelineItem.SupplementTimelineItem(
                            time = time,
                            supplement = supplementWithSettings,
                            resolvedScheduleEntry = null,
                            occurrenceId = null
                        )
                    }
                }
            }
        Log.d("Meow", "BuildTodayTimelineUseCase> supplementItems: ${supplementItems.size}")

        val supplementDoseLogItems =
            supplementDoseLogs.map { doseLog ->
                TimelineItem.SupplementDoseLogTimelineItem(
                    doseLogId = doseLog.id,
                    supplementId = doseLog.supplementId,
                    title = supplementTitleLookup[doseLog.supplementId] ?: "Supplement",
                    time = doseLog.timestamp.time,
                    amount = doseLog.actualServingTaken,
                    unit = doseLog.doseUnit.name,
                    scheduledTime = null
                )
            }
        Log.d("Meow", "BuildTodayTimelineUseCase> supplementDoseLogItems: ${supplementDoseLogItems.size}")

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

        val merged = (
                supplementItems +
                        supplementDoseLogItems +
                        mealItems +
                        importedMealItems +
                        activityItems
                ).sortedWith(
                compareBy<TimelineItem> { it.time }
                    .thenBy { itemTypeSortOrder(it) }
                    .thenBy { itemStablePrimaryKey(it) }
                    .thenBy { itemStableSecondaryKey(it) }
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

    /**
     * Cross-type ordering policy for rows that land on the same resolved time.
     *
     * This is intentionally a UI/user-sense rule, not a scheduling rule.
     */
    private fun itemTypeSortOrder(item: TimelineItem): Int =
        when (item) {
            is TimelineItem.SupplementTimelineItem -> 0
            is TimelineItem.MealTimelineItem -> 1
            is TimelineItem.ImportedMealTimelineItem -> 2
            is TimelineItem.ActivityTimelineItem -> 3
            is TimelineItem.SupplementDoseLogTimelineItem -> 4
        }

    /**
     * Stable identity-first tie-breaker.
     *
     * For items with identical resolved timeline time and identical cross-type
     * precedence, use the most stable business identity available.
     */
    private fun itemStablePrimaryKey(item: TimelineItem): String =
        when (item) {
            is TimelineItem.SupplementTimelineItem -> {
                val entry = item.resolvedScheduleEntry
                buildString {
                    append(item.supplement.supplement.id)
                    append("|")
                    append(entry?.scheduleId ?: Long.MAX_VALUE)
                    append("|")
                    append(entry?.sourceRowId ?: Long.MAX_VALUE)
                    append("|")
                    append(entry?.sortOrder ?: Int.MAX_VALUE)
                    append("|")
                    append(entry?.label ?: "")
                    append("|")
                    append(entry?.occurrenceId ?: item.occurrenceId ?: "")
                }
            }

            is TimelineItem.MealTimelineItem ->
                item.meal.id.toString()

            is TimelineItem.ImportedMealTimelineItem ->
                item.meal.groupingKey

            is TimelineItem.ActivityTimelineItem ->
                item.activity.id.toString()

            is TimelineItem.SupplementDoseLogTimelineItem ->
                item.doseLogId.toString()
        }

    /**
     * Secondary deterministic tie-breaker for edge cases where the primary key is
     * still equal or partially absent.
     *
     * This remains identity/meaning based and avoids hash-driven ordering.
     */
    private fun itemStableSecondaryKey(item: TimelineItem): String =
        when (item) {
            is TimelineItem.SupplementTimelineItem -> {
                val entry = item.resolvedScheduleEntry
                buildString {
                    append(entry?.timingType?.name ?: "")
                    append("|")
                    append(entry?.anchor?.name ?: "")
                    append("|")
                    append(item.time.toSecondOfDay())
                    append("|")
                    append(item.supplement.supplement.name)
                }
            }

            is TimelineItem.MealTimelineItem ->
                buildString {
                    append(item.meal.type.name)
                    append("|")
                    append(item.meal.name ?: "")
                    append("|")
                    append(item.meal.timestamp.time.toSecondOfDay())
                }

            is TimelineItem.ImportedMealTimelineItem ->
                buildString {
                    append(item.meal.type.name)
                    append("|")
                    append(item.meal.timestamp)
                }

            is TimelineItem.ActivityTimelineItem ->
                buildString {
                    append(item.activity.type.name)
                    append("|")
                    append(item.activity.start.time.toSecondOfDay())
                    append("|")
                    append(item.activity.end?.time?.toSecondOfDay() ?: Int.MAX_VALUE)
                }

            is TimelineItem.SupplementDoseLogTimelineItem ->
                buildString {
                    append(item.supplementId)
                    append("|")
                    append(item.time.toSecondOfDay())
                    append("|")
                    append(item.scheduledTime?.toSecondOfDay() ?: Int.MAX_VALUE)
                }
        }
}