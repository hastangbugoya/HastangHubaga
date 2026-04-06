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
import com.example.hastanghubaga.domain.model.meal.Meal
import com.example.hastanghubaga.domain.model.meal.MealLog
import com.example.hastanghubaga.domain.model.supplement.Supplement
import com.example.hastanghubaga.domain.model.supplement.SupplementDoseLog
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
        supplementIngredientsBySupplementId: Map<Long, List<TimelineItem.TimelineIngredientUi>> = emptyMap(),
        mealOccurrences: List<MealOccurrenceEntity> = emptyList(),
        meals: List<Meal> = emptyList(),
        mealLogs: List<MealLog> = emptyList(),
        importedMeals: List<AkImportedMealEntity> = emptyList(),
        activityOccurrences: List<ActivityOccurrenceEntity> = emptyList(),
        activities: List<Activity> = emptyList(),
        activityLogs: List<ActivityLog> = emptyList()
    ): List<TimelineItem> {

        Log.d("MEAL_RECON", "=== BuildTodayTimeline START date=$date ===")

        mealOccurrences.forEach {
            Log.d(
                "MEAL_RECON",
                "occurrence input id=${it.id} mealId=${it.mealId} plannedTime=${it.plannedTimeSeconds}"
            )
        }

        val supplementLookup = supplements.associateBy { it.id }
        val supplementTitleLookup = supplements.associate { it.id to it.name }

        val activeMeals = meals.filter { it.isActive }
        val mealLookup = activeMeals.associateBy { it.id }

        val activeActivities = activities.filter { it.isActive }
        val activityLookup = activeActivities.associateBy { it.id }

        val supplementOccurrenceTimeLookup =
            supplementOccurrences.associate {
                it.id to LocalTime.fromSecondOfDay(it.plannedTimeSeconds)
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
                    scheduledTime = plannedTime,
                    ingredients = supplementIngredientsBySupplementId[supplement.id].orEmpty()
                )
            }

        val satisfiedSupplementOccurrenceIds =
            supplementDoseLogs.mapNotNull { it.occurrenceId }.toSet()

        val filteredPlannedSupplementItems =
            plannedSupplementItems.filterNot {
                it.occurrenceId in satisfiedSupplementOccurrenceIds
            }

        val supplementDoseLogItems =
            supplementDoseLogs.map { log ->
                TimelineItem.SupplementDoseLogTimelineItem(
                    doseLogId = log.id,
                    supplementId = log.supplementId,
                    title = supplementTitleLookup[log.supplementId] ?: "Supplement",
                    time = log.timestamp.time,
                    amount = log.actualServingTaken,
                    unit = log.doseUnit.name,
                    scheduledTime = log.occurrenceId?.let(supplementOccurrenceTimeLookup::get),
                    ingredients = supplementIngredientsBySupplementId[log.supplementId].orEmpty()
                )
            }

        val mergedMealItems =
            buildMergedMealTimelineItems(
                mealOccurrences,
                mealLookup,
                mealLogs
            )

        val importedMealItems =
            importedMeals.map {
                TimelineItem.ImportedMealTimelineItem(
                    time = Instant.fromEpochMilliseconds(it.timestamp)
                        .toLocalDateTime(DomainTimePolicy.localTimeZone)
                        .time,
                    meal = it
                )
            }

        val mergedActivityItems =
            buildMergedActivityTimelineItems(
                activityOccurrences,
                activityLookup,
                activityLogs
            )

        val merged = (
                filteredPlannedSupplementItems +
                        supplementDoseLogItems +
                        mergedMealItems +
                        importedMealItems +
                        mergedActivityItems
                ).sortedWith(
                compareBy<TimelineItem> { it.time }
                    .thenBy { itemTypeSortOrder(it) }
                    .thenBy { itemStablePrimaryKey(it) }
                    .thenBy { itemStableSecondaryKey(it) }
            )

        Log.d("MEAL_RECON", "=== FINAL MEAL ITEMS ===")
        merged.filterIsInstance<TimelineItem.MealTimelineItem>().forEach {
            Log.d(
                "MEAL_RECON",
                "final meal item type=${it.meal.type} occurrenceId=${it.occurrenceId} isCompleted=${it.isCompleted} time=${it.time}"
            )
        }

        val upcomingItems =
            merged.mapNotNull { it.toUpcomingSchedule(date) }

        buildWidgetDailySnapshotUseCase(date)
        upcomingScheduleRepository.replaceAll(upcomingItems)

        return merged
    }

    private fun buildMergedMealTimelineItems(
        occurrences: List<MealOccurrenceEntity>,
        lookup: Map<Long, Meal>,
        logs: List<MealLog>
    ): List<TimelineItem.MealTimelineItem> {

        val planned =
            occurrences.mapNotNull { o ->
                val meal = lookup[o.mealId] ?: return@mapNotNull null
                val t = LocalTime.fromSecondOfDay(o.plannedTimeSeconds)

                Log.d(
                    "MEAL_RECON",
                    "BUILD planned meal occurrenceId=${o.id} mealId=${o.mealId} type=${meal.type} time=$t"
                )

                TimelineItem.MealTimelineItem(
                    time = t,
                    occurrenceId = o.id,
                    meal = meal,
                    scheduledTime = t,
                    isCompleted = false,
                    resolvedAnchor = resolveMealAnchorUseCase(meal)
                )
            }

        val logsByOccurrenceId =
            logs.mapNotNull { log ->
                val occurrenceId = log.occurrenceId ?: return@mapNotNull null
                occurrenceId to log
            }.toMap()

        val mergedPlanned =
            planned.map { plannedItem ->
                val log = logsByOccurrenceId[plannedItem.occurrenceId]

                if (log == null) {
                    plannedItem
                } else {
                    val meal =
                        log.mealId?.let(lookup::get) ?: plannedItem.meal

                    val actualTime = log.start.time

                    Log.d(
                        "MEAL_RECON",
                        "MERGE completed occurrenceId=${plannedItem.occurrenceId} -> actualTime=$actualTime"
                    )

                    TimelineItem.MealTimelineItem(
                        time = actualTime,
                        occurrenceId = plannedItem.occurrenceId,
                        meal = meal,
                        scheduledTime = plannedItem.scheduledTime,
                        isCompleted = true,
                        resolvedAnchor = resolveMealAnchorUseCase(meal)
                    )
                }
            }

        val adHocLogs =
            logs.filter { it.occurrenceId == null }
                .mapNotNull { log ->
                    val meal = log.mealId?.let(lookup::get) ?: return@mapNotNull null
                    val actualTime = log.start.time

                    Log.d("MEAL_RECON", "ADHOC log id=${log.id} time=$actualTime")

                    TimelineItem.MealTimelineItem(
                        time = actualTime,
                        occurrenceId = "adhoc_${log.id}",
                        meal = meal,
                        scheduledTime = actualTime,
                        isCompleted = true,
                        resolvedAnchor = resolveMealAnchorUseCase(meal)
                    )
                }

        return mergedPlanned + adHocLogs
    }

    private fun buildMergedActivityTimelineItems(
        occurrences: List<ActivityOccurrenceEntity>,
        lookup: Map<Long, Activity>,
        logs: List<ActivityLog>
    ): List<TimelineItem.ActivityTimelineItem> {

        val planned =
            occurrences.mapNotNull { occurrence ->
                val activity = lookup[occurrence.activityId] ?: return@mapNotNull null
                val plannedTime = LocalTime.fromSecondOfDay(occurrence.plannedTimeSeconds)

                TimelineItem.ActivityTimelineItem(
                    time = plannedTime,
                    occurrenceId = occurrence.id,
                    activityId = activity.id,
                    title = activity.type.name,
                    scheduledTime = plannedTime,
                    isCompleted = false
                )
            }

        val logsByOccurrenceId =
            logs.mapNotNull { log ->
                val occurrenceId = log.occurrenceId ?: return@mapNotNull null
                occurrenceId to log
            }.toMap()

        val mergedPlanned =
            planned.map { plannedItem ->
                val log = logsByOccurrenceId[plannedItem.occurrenceId]

                if (log == null) {
                    plannedItem
                } else {
                    val activity =
                        log.activityId?.let(lookup::get)

                    val actualTime = log.start.time

                    TimelineItem.ActivityTimelineItem(
                        time = actualTime,
                        occurrenceId = plannedItem.occurrenceId,
                        activityId = activity?.id ?: plannedItem.activityId,
                        title = activity?.type?.name ?: plannedItem.title,
                        scheduledTime = plannedItem.scheduledTime,
                        isCompleted = true
                    )
                }
            }

        val adHocLogs =
            logs.filter { it.occurrenceId == null }
                .mapNotNull { log ->
                    val activity = log.activityId?.let(lookup::get) ?: return@mapNotNull null
                    val actualTime = log.start.time

                    TimelineItem.ActivityTimelineItem(
                        time = actualTime,
                        occurrenceId = "adhoc_${log.id}",
                        activityId = activity.id,
                        title = activity.type.name,
                        scheduledTime = actualTime,
                        isCompleted = true
                    )
                }

        return mergedPlanned + adHocLogs
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
            is TimelineItem.MealTimelineItem ->
                "${item.meal.id}|${item.occurrenceId}|${item.scheduledTime.toSecondOfDay()}"

            is TimelineItem.SupplementTimelineItem ->
                "${item.supplementId}|${item.occurrenceId}|${item.scheduledTime.toSecondOfDay()}"

            is TimelineItem.ActivityTimelineItem ->
                "${item.activityId}|${item.occurrenceId}|${item.scheduledTime.toSecondOfDay()}"

            is TimelineItem.ImportedMealTimelineItem ->
                item.meal.groupingKey

            is TimelineItem.SupplementDoseLogTimelineItem ->
                item.doseLogId.toString()
        }

    private fun itemStableSecondaryKey(item: TimelineItem): String =
        when (item) {
            is TimelineItem.MealTimelineItem ->
                "${item.meal.type.name}|${item.meal.name}|${item.time.toSecondOfDay()}|${if (item.isCompleted) "C" else "P"}"

            is TimelineItem.SupplementTimelineItem ->
                "${item.title}|${item.time.toSecondOfDay()}|${item.defaultUnit}|${item.suggestedDose}"

            is TimelineItem.ActivityTimelineItem ->
                "${item.title}|${item.time.toSecondOfDay()}|${if (item.isCompleted) "C" else "P"}"

            is TimelineItem.ImportedMealTimelineItem ->
                "${item.meal.type}|${item.meal.timestamp}"

            is TimelineItem.SupplementDoseLogTimelineItem ->
                "${item.supplementId}|${item.time.toSecondOfDay()}"
        }
}