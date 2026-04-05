package com.example.hastanghubaga.domain.usecase.todaytimeline

import android.util.Log
import com.example.hastanghubaga.data.local.entity.activity.ActivityOccurrenceEntity
import com.example.hastanghubaga.data.local.entity.meal.AkImportedMealEntity
import com.example.hastanghubaga.data.local.entity.meal.MealOccurrenceEntity
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
 * Current native HH meal model:
 * - planned rows come from persisted meal occurrences
 * - imported AK meals are still concrete historical rows with timestamps
 * - native HH actual meal log reconciliation is not wired yet
 *
 * This use case trusts upstream time resolution and only performs:
 * - mapping to timeline rows
 * - planned-vs-actual reconciliation where available
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
        mealOccurrences: List<MealOccurrenceEntity> = emptyList(),
        meals: List<Meal> = emptyList(),
        importedMeals: List<AkImportedMealEntity> = emptyList(),
        activityOccurrences: List<ActivityOccurrenceEntity> = emptyList(),
        activities: List<Activity> = emptyList(),
        activityLogs: List<ActivityLog> = emptyList()
    ): List<TimelineItem> {
        val supplementLookup = supplements.associateBy { it.id }
        val supplementTitleLookup = supplements.associate { it.id to it.name }

        val activeMeals = meals.filter { it.isActive }
        val mealLookup = activeMeals.associateBy { it.id }

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

        Log.d("MEAL_RECON", "meals input size=${meals.size}")
        meals.forEachIndexed { index, meal ->
            Log.d(
                "MEAL_RECON",
                "meals#$index > id=${meal.id} name=${meal.name} type=${meal.type} treatAsAnchor=${meal.treatAsAnchor} isActive=${meal.isActive}"
            )
        }
        meals.groupingBy { it.type }.eachCount()
            .toSortedMap(compareBy { it.name })
            .forEach { (type, count) ->
                Log.d("MEAL_RECON", "mealTemplatesByType[$type]=$count")
            }

        activeMeals.forEach { meal ->
            val resolvedAnchor = resolveMealAnchorUseCase(meal)
            if (resolvedAnchor != null) {
                Log.d(
                    "MealAnchor",
                    "Meal template '${meal.name}' resolved anchor=$resolvedAnchor type=${meal.type} override=${meal.treatAsAnchor}"
                )
            }
        }

        Log.d("MEAL_RECON", "mealOccurrences input size=${mealOccurrences.size}")
        mealOccurrences.forEachIndexed { index, occurrence ->
            val matchedMeal = mealLookup[occurrence.mealId]
            Log.d(
                "MEAL_RECON",
                "mealOccurrences#$index > id=${occurrence.id} mealId=${occurrence.mealId} scheduleId=${occurrence.scheduleId} date=${occurrence.date} plannedTimeSeconds=${occurrence.plannedTimeSeconds} sourceType=${occurrence.sourceType} isDeleted=${occurrence.isDeleted} matchedMealType=${matchedMeal?.type} matchedMealName=${matchedMeal?.name}"
            )
        }
        mealOccurrences.groupingBy { occurrence ->
            mealLookup[occurrence.mealId]?.type?.name ?: "UNMATCHED"
        }.eachCount()
            .toSortedMap()
            .forEach { (typeName, count) ->
                Log.d("MEAL_RECON", "mealOccurrencesByType[$typeName]=$count")
            }

        val mealItems =
            mealOccurrences.mapNotNull { occurrence ->
                val meal = mealLookup[occurrence.mealId] ?: return@mapNotNull null
                val plannedTime = LocalTime.fromSecondOfDay(occurrence.plannedTimeSeconds)

                TimelineItem.MealTimelineItem(
                    time = plannedTime,
                    meal = meal
                )
            }
        Log.d("Meow", "BuildTodayTimelineUseCase> mealItems: ${mealItems.size}")
        mealItems.forEachIndexed { index, item ->
            Log.d(
                "MEAL_RECON",
                "mealItems#$index > mealId=${item.meal.id} name=${item.meal.name} type=${item.meal.type} time=${item.time}"
            )
        }
        mealItems.groupingBy { it.meal.type }.eachCount()
            .toSortedMap(compareBy { it.name })
            .forEach { (type, count) ->
                Log.d("MEAL_RECON", "mealTimelineItemsByType[$type]=$count")
            }

        Log.d("ImportDebug", "importedMeals input size=${importedMeals.size}")
        importedMeals.forEachIndexed { index, importedMeal ->
            Log.d(
                "ImportDebug",
                "importedMeals#$index > groupingKey=${importedMeal.groupingKey} " +
                        "logDateIso=${importedMeal.logDateIso} " +
                        "type=${importedMeal.type} " +
                        "timestamp=${importedMeal.timestamp} " +
                        "notes=${importedMeal.notes}"
            )
        }
        importedMeals.groupingBy { it.type }.eachCount()
            .toSortedMap(compareBy { it.name })
            .forEach { (type, count) ->
                Log.d("MEAL_RECON", "importedMealsByType[$type]=$count")
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

        importedMealItems.forEachIndexed { index, importedMealItem ->
            Log.d(
                "MEAL_RECON",
                "importedMealItems#$index > time=${importedMealItem.time} groupingKey=${importedMealItem.meal.groupingKey} type=${importedMealItem.meal.type}"
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

        val mergedNativeMealItems = merged.filterIsInstance<TimelineItem.MealTimelineItem>()
        Log.d("MEAL_RECON", "merged native meal items size=${mergedNativeMealItems.size}")
        mergedNativeMealItems.groupingBy { it.meal.type }.eachCount()
            .toSortedMap(compareBy { it.name })
            .forEach { (type, count) ->
                Log.d("MEAL_RECON", "mergedNativeMealItemsByType[$type]=$count")
            }

        val mergedImportedMealItems = merged.filterIsInstance<TimelineItem.ImportedMealTimelineItem>()
        Log.d("MEAL_RECON", "merged imported meal items size=${mergedImportedMealItems.size}")
        mergedImportedMealItems.groupingBy { it.meal.type }.eachCount()
            .toSortedMap(compareBy { it.name })
            .forEach { (type, count) ->
                Log.d("MEAL_RECON", "mergedImportedMealItemsByType[$type]=$count")
            }

        val upcomingItems =
            merged.mapNotNull<TimelineItem, UpcomingSchedule> { item ->
                item.toUpcomingSchedule(date = date)
            }
        Log.d("Meow", "BuildTodayTimelineUseCase> upcomingItems: ${upcomingItems.size}")

        buildWidgetDailySnapshotUseCase(date)
        upcomingScheduleRepository.replaceAll(upcomingItems)

        return merged
    }

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

    private fun itemTypeSortOrder(item: TimelineItem): Int =
        when (item) {
            is TimelineItem.SupplementTimelineItem -> 0
            is TimelineItem.MealTimelineItem -> 1
            is TimelineItem.ImportedMealTimelineItem -> 2
            is TimelineItem.ActivityTimelineItem -> 3
            is TimelineItem.SupplementDoseLogTimelineItem -> 4
        }

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
                buildString {
                    append(item.meal.id)
                    append("|")
                    append(item.time.toSecondOfDay())
                }

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
                    append(item.meal.name)
                    append("|")
                    append(item.time.toSecondOfDay())
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