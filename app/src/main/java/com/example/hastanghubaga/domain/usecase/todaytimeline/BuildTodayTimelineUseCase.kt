package com.example.hastanghubaga.domain.usecase.todaytimeline

import android.util.Log
import com.example.hastanghubaga.data.local.entity.meal.AkImportedMealEntity
import com.example.hastanghubaga.data.local.entity.supplement.SupplementOccurrenceEntity
import com.example.hastanghubaga.data.local.entity.supplement.toDisplayCase
import com.example.hastanghubaga.data.local.mappers.toUpcomingSchedule
import com.example.hastanghubaga.domain.model.activity.Activity
import com.example.hastanghubaga.domain.model.meal.Meal
import com.example.hastanghubaga.domain.model.supplement.Supplement
import com.example.hastanghubaga.domain.model.supplement.SupplementDoseLog
import com.example.hastanghubaga.domain.model.timeline.UpcomingSchedule
import com.example.hastanghubaga.domain.repository.time.UpcomingScheduleRepository
import com.example.hastanghubaga.domain.time.DomainTimePolicy
import com.example.hastanghubaga.domain.usecase.meal.ResolveMealAnchorUseCase
import com.example.hastanghubaga.ui.timeline.TimelineItem
import com.example.hastanghubaga.ui.util.asDisplayTextNonComposable
import com.example.hastanghubaga.widget.snapshot.BuildWidgetDailySnapshot
import javax.inject.Inject
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.toLocalDateTime

/**
 * Builds a single, chronologically ordered timeline for the selected day.
 *
 * Canonical supplement model:
 * - planned rows come from persisted supplement occurrences
 * - actual rows come from supplement dose logs
 * - reconciliation happens by occurrenceId
 *
 * Planned row rule:
 * - show the planned occurrence if it has not yet been satisfied by a linked log
 *
 * Actual row rule:
 * - always show the actual user-declared log event
 * - if the log links to a planned occurrence, the matching planned row is suppressed
 * - if the log has no occurrenceId, it is treated as an extra/manual/ad-hoc event
 *
 * Other timeline content:
 * - native HH meals
 * - imported meals
 * - activities
 *
 * This use case trusts upstream time resolution and only performs:
 * - mapping to timeline rows
 * - planned-vs-actual supplement reconciliation
 * - deterministic merge/sort
 * - simplified upcoming schedule snapshot persistence
 */
class BuildTodayTimelineUseCase @Inject constructor(
    private val upcomingScheduleRepository: UpcomingScheduleRepository,
    private val buildWidgetDailySnapshotUseCase: BuildWidgetDailySnapshot,
    private val resolveMealAnchorUseCase: ResolveMealAnchorUseCase
) {
    suspend operator fun invoke(
        date: LocalDate,
        supplementOccurrences: List<SupplementOccurrenceEntity>,
        supplements: List<Supplement>,
        supplementDoseLogs: List<SupplementDoseLog> = emptyList(),
        meals: List<Meal> = emptyList(),
        importedMeals: List<AkImportedMealEntity> = emptyList(),
        activities: List<Activity> = emptyList()
    ): List<TimelineItem> {
        val supplementLookup = supplements.associateBy { it.id }
        val supplementTitleLookup = supplements.associate { it.id to it.name }

        val occurrenceTimeLookup =
            supplementOccurrences.associate { occurrence ->
                occurrence.id to LocalTime.fromSecondOfDay(occurrence.plannedTimeSeconds)
            }

        val plannedSupplementItems =
            supplementOccurrences.mapNotNull { occurrence ->
                val supplement = supplementLookup[occurrence.supplementId] ?: return@mapNotNull null

                val plannedTime = LocalTime.fromSecondOfDay(occurrence.plannedTimeSeconds)
                val subtitle =
                    "${supplement.recommendedServingSize.asDisplayTextNonComposable()} " +
                            supplement.recommendedDoseUnit.toDisplayCase(
                                supplement.recommendedServingSize
                            )

                TimelineItem.SupplementTimelineItem(
                    time = plannedTime,
                    occurrenceId = occurrence.id,
                    supplementId = supplement.id,
                    title = supplement.name,
                    subtitle = subtitle,
                    defaultUnit = supplement.recommendedDoseUnit,
                    suggestedDose = supplement.recommendedServingSize,
                    doseState = null,
                    scheduledTime = plannedTime,
                    isTaken = false
                )
            }
        Log.d("Meow", "BuildTodayTimelineUseCase> plannedSupplementItems: ${plannedSupplementItems.size}")

        val satisfiedOccurrenceIds =
            supplementDoseLogs
                .mapNotNull { it.occurrenceId }
                .toSet()
        Log.d(
            "Meow",
            "BuildTodayTimelineUseCase> satisfiedOccurrenceIds: ${satisfiedOccurrenceIds.size}"
        )

        val filteredPlannedSupplementItems =
            plannedSupplementItems.filterNot { item ->
                item.occurrenceId in satisfiedOccurrenceIds
            }
        Log.d(
            "Meow",
            "BuildTodayTimelineUseCase> filteredPlannedSupplementItems: ${filteredPlannedSupplementItems.size}"
        )

        val supplementDoseLogItems =
            supplementDoseLogs.map { doseLog ->
                TimelineItem.SupplementDoseLogTimelineItem(
                    doseLogId = doseLog.id,
                    supplementId = doseLog.supplementId,
                    title = supplementTitleLookup[doseLog.supplementId] ?: "Supplement",
                    time = doseLog.timestamp.time,
                    amount = doseLog.actualServingTaken,
                    unit = doseLog.doseUnit.name,
                    scheduledTime = doseLog.occurrenceId?.let(occurrenceTimeLookup::get)
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
                filteredPlannedSupplementItems +
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
            is TimelineItem.SupplementTimelineItem ->
                buildString {
                    append(item.supplementId)
                    append("|")
                    append(item.occurrenceId)
                    append("|")
                    append(item.scheduledTime.toSecondOfDay())
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
            is TimelineItem.SupplementTimelineItem ->
                buildString {
                    append(item.title)
                    append("|")
                    append(item.time.toSecondOfDay())
                    append("|")
                    append(item.defaultUnit.name)
                    append("|")
                    append(item.suggestedDose)
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