package com.example.hastanghubaga.domain.usecase.todaytimeline

import android.util.Log
import com.example.hastanghubaga.data.local.entity.activity.ActivityOccurrenceEntity
import com.example.hastanghubaga.data.local.entity.meal.AkImportedMealEntity
import com.example.hastanghubaga.data.local.entity.supplement.SupplementOccurrenceEntity
import com.example.hastanghubaga.data.local.entity.supplement.toDisplayCase
import com.example.hastanghubaga.data.local.mappers.toUpcomingSchedule
import com.example.hastanghubaga.domain.model.activity.Activity
import com.example.hastanghubaga.domain.model.activity.ActivityLog
import com.example.hastanghubaga.domain.model.activity.ActivityType
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
 * Canonical activity model:
 * - planned rows come from persisted activity occurrences
 * - actual rows come from activity logs
 * - reconciliation happens by occurrenceId
 *
 * Planned activity row rule:
 * - show the planned occurrence if it has not yet been satisfied by a linked log
 *
 * Actual activity row rule:
 * - if a log links to a planned occurrence, that linked log should overwrite
 *   the planned row for display purposes so the occurrence appears only once
 * - if a log has no occurrenceId, it is treated as an extra/manual/ad-hoc event
 *   and is appended as its own standalone row
 *
 * Important guardrail:
 * - inactive activity templates must not produce planned rows
 * - actual logs are historical truth and may still appear even if the template
 *   later becomes inactive
 *
 * Other timeline content:
 * - native HH meals
 * - imported meals
 *
 * This use case trusts upstream time resolution and only performs:
 * - mapping to timeline rows
 * - planned-vs-actual reconciliation
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
        activityOccurrences: List<ActivityOccurrenceEntity> = emptyList(),
        activities: List<Activity> = emptyList(),
        activityLogs: List<ActivityLog> = emptyList()
    ): List<TimelineItem> {
        val supplementLookup = supplements.associateBy { it.id }
        val supplementTitleLookup = supplements.associate { it.id to it.name }

        val activeActivities = activities.filter { it.isActive }
        val activityLookup = activeActivities.associateBy { it.id }

        val supplementOccurrenceTimeLookup =
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

        val satisfiedSupplementOccurrenceIds =
            supplementDoseLogs
                .mapNotNull { it.occurrenceId }
                .toSet()
        Log.d(
            "Meow",
            "BuildTodayTimelineUseCase> satisfiedSupplementOccurrenceIds: ${satisfiedSupplementOccurrenceIds.size}"
        )

        val filteredPlannedSupplementItems =
            plannedSupplementItems.filterNot { item ->
                item.occurrenceId in satisfiedSupplementOccurrenceIds
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
                    scheduledTime = doseLog.occurrenceId?.let(supplementOccurrenceTimeLookup::get)
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
                    meal = importedMeal
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

        activityOccurrences.forEachIndexed { index, occurrence ->
            Log.d("ACTIVITY_RECON", "activityOccurrences#$index > $occurrence")
        }

        activityLogs.forEachIndexed { index, log ->
            Log.d("ACTIVITY_RECON", "activityLogs#$index > $log")
        }

        val mergedActivityItems =
            buildMergedActivityTimelineItems(
                activityOccurrences = activityOccurrences,
                activityLookup = activityLookup,
                activityLogs = activityLogs
            )
        Log.d("Meow", "BuildTodayTimelineUseCase> mergedActivityItems: ${mergedActivityItems.size}")

        val merged = (
                filteredPlannedSupplementItems +
                        supplementDoseLogItems +
                        mealItems +
                        importedMealItems +
                        mergedActivityItems
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
     * Activity timeline reconciliation.
     *
     * NOTE TO FUTURE DEVS / AI ASSISTANTS:
     *
     * Activities intentionally use an identity-keyed merge instead of the older
     * "filter planned + append actual" approach.
     *
     * Why:
     * - A planned activity occurrence and its linked activity log represent the
     *   same logical timeline slot.
     * - Showing both rows causes duplicate cards and breaks the intended
     *   completed-vs-planned behavior.
     *
     * Canonical rule:
     * - occurrenceId is the identity of a planned activity slot
     * - planned item is inserted first
     * - linked logged item with the same occurrenceId overwrites that planned item
     * - unlinked logs (occurrenceId == null) are appended separately as ad-hoc rows
     *
     * Data-structure invariant:
     * - at most one rendered activity row per linked occurrenceId
     *
     * Important:
     * - this is intentionally implemented as a LinkedHashMap keyed by occurrenceId
     *   so the data structure itself enforces the no-duplicate-occurrence rule
     * - do not revert this back to filterNot + append for activities unless the
     *   canonical ownership model changes
     *
     * Forward-looking note:
     * - once this pattern is proven solid for activities, the same
     *   identity-keyed overwrite/merge approach can be applied to supplements
     *   and later to meals if/when they gain planned-vs-actual reconciliation
     */
    private fun buildMergedActivityTimelineItems(
        activityOccurrences: List<ActivityOccurrenceEntity>,
        activityLookup: Map<Long, Activity>,
        activityLogs: List<ActivityLog>
    ): List<TimelineItem.ActivityTimelineItem> {
        val plannedActivityItems =
            activityOccurrences.mapNotNull { occurrence ->
                val activity = activityLookup[occurrence.activityId] ?: return@mapNotNull null
                val plannedTime = LocalTime.fromSecondOfDay(occurrence.plannedTimeSeconds)

                TimelineItem.ActivityTimelineItem(
                    time = plannedTime,
                    occurrenceId = occurrence.id,
                    activityId = activity.id,
                    title = activity.type.toDisplayLabel(),
                    subtitle = activity.notes,
                    isWorkout = occurrence.isWorkout,
                    scheduledTime = plannedTime,
                    isCompleted = false
                )
            }

        Log.d("Meow", "BuildTodayTimelineUseCase> plannedActivityItems: ${plannedActivityItems.size}")

        val plannedByOccurrenceId =
            plannedActivityItems.associateBy { it.occurrenceId }

        val linkedLoggedActivityItems =
            activityLogs.mapNotNull { log ->
                val occurrenceId = log.occurrenceId ?: return@mapNotNull null
                val planned = plannedByOccurrenceId[occurrenceId]

                TimelineItem.ActivityTimelineItem(
                    time = log.start.time,
                    occurrenceId = occurrenceId,
                    activityId = log.activityId ?: planned?.activityId ?: -1L,
                    title = log.activityType.toDisplayLabel(),
                    subtitle = log.notes ?: planned?.subtitle,
                    isWorkout = planned?.isWorkout ?: false,
                    scheduledTime = planned?.scheduledTime ?: log.start.time,
                    isCompleted = true
                )
            }

        Log.d(
            "Meow",
            "BuildTodayTimelineUseCase> linkedLoggedActivityItems: ${linkedLoggedActivityItems.size}"
        )

        val standaloneActualActivityItems =
            activityLogs
                .filter { it.occurrenceId == null }
                .map { log ->
                    val actualTime = log.start.time

                    TimelineItem.ActivityTimelineItem(
                        time = actualTime,
                        occurrenceId = buildActualActivityTimelineId(
                            activityId = log.activityId,
                            time = actualTime
                        ),
                        activityId = log.activityId ?: -1L,
                        title = log.activityType.toDisplayLabel(),
                        subtitle = log.notes,
                        isWorkout = false,
                        scheduledTime = actualTime,
                        isCompleted = true
                    )
                }

        Log.d(
            "Meow",
            "BuildTodayTimelineUseCase> standaloneActualActivityItems: ${standaloneActualActivityItems.size}"
        )

        val mergedByOccurrenceId = linkedMapOf<String, TimelineItem.ActivityTimelineItem>()

        plannedActivityItems.forEach { planned ->
            mergedByOccurrenceId[planned.occurrenceId] = planned
        }

        linkedLoggedActivityItems.forEach { logged ->
            mergedByOccurrenceId[logged.occurrenceId] = logged
        }

        val mergedActivityItems =
            mergedByOccurrenceId.values.toList() + standaloneActualActivityItems

        Log.d(
            "Meow",
            "BuildTodayTimelineUseCase> mergedActivityItems(after overwrite + standalone append): ${mergedActivityItems.size}"
        )

        return mergedActivityItems
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
                buildString {
                    append(item.activityId)
                    append("|")
                    append(item.occurrenceId)
                    append("|")
                    append(item.scheduledTime.toSecondOfDay())
                }

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
                    append(item.title)
                    append("|")
                    append(item.time.toSecondOfDay())
                    append("|")
                    append(if (item.isWorkout) "W" else "N")
                    append("|")
                    append(if (item.isCompleted) "C" else "P")
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

    private fun buildActualActivityTimelineId(
        activityId: Long?,
        time: LocalTime
    ): String {
        return buildString {
            append("actual")
            append("|")
            append(activityId ?: "none")
            append("|")
            append(time.toSecondOfDay())
        }
    }
}

private fun ActivityType.toDisplayLabel(): String =
    name
        .lowercase()
        .split("_")
        .joinToString(" ") { word -> word.replaceFirstChar { it.uppercase() } }