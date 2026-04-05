package com.example.hastanghubaga.domain.usecase.supplement

import com.example.hastanghubaga.data.local.dao.supplement.EventTimeDao
import com.example.hastanghubaga.data.local.dao.supplement.SupplementScheduleDao
import com.example.hastanghubaga.data.local.entity.meal.AkImportedMealEntity
import com.example.hastanghubaga.data.local.entity.meal.MealType
import com.example.hastanghubaga.data.local.entity.supplement.DoseAnchorType
import com.example.hastanghubaga.data.local.entity.supplement.ScheduleRecurrenceType
import com.example.hastanghubaga.data.local.entity.supplement.ScheduleTimingType
import com.example.hastanghubaga.data.local.entity.supplement.SupplementOccurrenceEntity
import com.example.hastanghubaga.data.local.entity.supplement.SupplementOccurrenceSourceType
import com.example.hastanghubaga.data.local.entity.supplement.SupplementScheduleAnchoredTimeEntity
import com.example.hastanghubaga.data.local.entity.supplement.SupplementScheduleEntity
import com.example.hastanghubaga.data.local.entity.supplement.SupplementScheduleFixedTimeEntity
import com.example.hastanghubaga.domain.model.meal.Meal
import com.example.hastanghubaga.domain.repository.activity.ActivityRepository
import com.example.hastanghubaga.domain.repository.supplement.SupplementRepository
import com.example.hastanghubaga.domain.schedule.model.AnchorDateKey
import com.example.hastanghubaga.domain.schedule.model.AnchorTimeContext
import com.example.hastanghubaga.domain.schedule.model.TimeAnchor
import com.example.hastanghubaga.domain.schedule.model.WorkoutAnchorWindow
import com.example.hastanghubaga.domain.schedule.timing.ApplyAnchorOffsetUseCase
import com.example.hastanghubaga.domain.schedule.timing.ResolveAnchorTimeUseCase
import com.example.hastanghubaga.domain.time.DomainTimePolicy
import com.example.hastanghubaga.domain.usecase.meal.ResolveMealAnchorUseCase
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toLocalDateTime

/**
 * Builds the deterministic PLANNED supplement occurrence set for a single date.
 *
 * Canonical rules:
 * - Only persisted supplement schedules are considered authoritative here
 * - Legacy supplement-level timing / recurrence fields are intentionally ignored
 * - One fixed-time child row => one planned occurrence
 * - One anchored child row that resolves successfully => one planned occurrence
 * - occurrenceId is deterministic and stable for the same:
 *   date + supplementId + scheduleId + sourceRowId + resolved time + sortOrder
 *
 * Anchor resolution rules:
 * - Concrete meal anchors currently come from imported AK meals only
 * - Native HH Meal domain objects are now reusable templates and do NOT carry
 *   concrete timestamps, so they cannot act as same-day anchor providers here
 * - Workout anchors use same-day workout activities
 * - If a concrete meal/workout time is unavailable, resolution falls back to:
 *   date override -> day-of-week override -> default anchor time
 *
 * Deterministic first-pass meal rule:
 * - If multiple imported meals resolve to the same anchor on the same day,
 *   the earliest meal time wins for that anchor.
 */
class BuildPlannedSupplementOccurrencesForDateUseCase @Inject constructor(
    private val supplementRepository: SupplementRepository,
    private val supplementScheduleDao: SupplementScheduleDao,
    private val activityRepository: ActivityRepository,
    private val eventTimeDao: EventTimeDao,
    private val resolveAnchorTimeUseCase: ResolveAnchorTimeUseCase,
    private val applyAnchorOffsetUseCase: ApplyAnchorOffsetUseCase,
    private val resolveMealAnchorUseCase: ResolveMealAnchorUseCase
) {

    suspend operator fun invoke(
        date: LocalDate,
        meals: List<Meal> = emptyList(),
        importedMeals: List<AkImportedMealEntity> = emptyList()
    ): List<SupplementOccurrenceEntity> {
        val supplements = supplementRepository.getActiveSupplements().first()
        val activities = activityRepository.observeActivitiesForDate(date).first()

        val workoutAnchors = activities
            .asSequence()
            .filter { it.isWorkout }
            .map { activity ->
                WorkoutAnchorWindow(
                    activityId = activity.id,
                    startTime = activity.start.time,
                    endTime = activity.end?.time,
                    label = activity.notes
                )
            }
            .sortedBy { it.startTime }
            .toList()

        val mealAnchorTimes = buildMealAnchorTimes(
            date = date,
            meals = meals,
            importedMeals = importedMeals
        )

        val anchorContext = buildAnchorTimeContext(
            date = date,
            workoutAnchors = workoutAnchors
        )

        val occurrences = supplements
            .flatMap { supplement ->
                val schedules = supplementScheduleDao
                    .getSchedulesForSupplement(supplement.id)
                    .asSequence()
                    .filter { it.isEnabled }
                    .filter { it.isActiveOn(date) }
                    .sortedBy { it.id }
                    .toList()

                schedules.flatMap { schedule ->
                    when (schedule.timingType) {
                        ScheduleTimingType.FIXED ->
                            buildFixedOccurrences(
                                date = date,
                                supplementId = supplement.id,
                                schedule = schedule
                            )

                        ScheduleTimingType.ANCHORED ->
                            buildAnchoredOccurrences(
                                date = date,
                                supplementId = supplement.id,
                                schedule = schedule,
                                anchorContext = anchorContext,
                                mealAnchorTimes = mealAnchorTimes
                            )
                    }
                }
            }
            .distinctBy { it.id }
            .sortedWith(
                compareBy<SupplementOccurrenceEntity>({ it.plannedTimeSeconds })
                    .thenBy { it.id }
            )

        return occurrences
    }

    private suspend fun buildFixedOccurrences(
        date: LocalDate,
        supplementId: Long,
        schedule: SupplementScheduleEntity
    ): List<SupplementOccurrenceEntity> {
        val rows = supplementScheduleDao
            .getFixedTimesForSchedule(schedule.id)
            .sortedWith(
                compareBy<SupplementScheduleFixedTimeEntity>(
                    { it.sortOrder },
                    { it.time.hour },
                    { it.time.minute },
                    { it.id }
                )
            )

        return rows.map { row ->
            SupplementOccurrenceEntity(
                id = buildOccurrenceId(
                    date = date,
                    supplementId = supplementId,
                    scheduleId = schedule.id,
                    sourceRowId = row.id,
                    time = row.time,
                    sortOrder = row.sortOrder
                ),
                supplementId = supplementId,
                scheduleId = schedule.id,
                date = date.toString(),
                plannedTimeSeconds = row.time.toSecondOfDay(),
                sourceType = SupplementOccurrenceSourceType.SCHEDULED,
                isDeleted = false
            )
        }
    }

    private suspend fun buildAnchoredOccurrences(
        date: LocalDate,
        supplementId: Long,
        schedule: SupplementScheduleEntity,
        anchorContext: AnchorTimeContext,
        mealAnchorTimes: Map<TimeAnchor, LocalTime>
    ): List<SupplementOccurrenceEntity> {
        val rows = supplementScheduleDao
            .getAnchoredTimesForSchedule(schedule.id)
            .sortedWith(
                compareBy<SupplementScheduleAnchoredTimeEntity>(
                    { it.sortOrder },
                    { it.id }
                )
            )

        return rows.mapNotNull { row ->
            val resolvedTime = resolveAnchoredTime(
                anchor = row.anchor,
                offsetMinutes = row.offsetMinutes,
                context = anchorContext,
                mealAnchorTimes = mealAnchorTimes
            ) ?: return@mapNotNull null

            SupplementOccurrenceEntity(
                id = buildOccurrenceId(
                    date = date,
                    supplementId = supplementId,
                    scheduleId = schedule.id,
                    sourceRowId = row.id,
                    time = resolvedTime,
                    sortOrder = row.sortOrder
                ),
                supplementId = supplementId,
                scheduleId = schedule.id,
                date = date.toString(),
                plannedTimeSeconds = resolvedTime.toSecondOfDay(),
                sourceType = SupplementOccurrenceSourceType.SCHEDULED,
                isDeleted = false
            )
        }
    }

    /**
     * Meal anchors take precedence when a concrete same-day meal timestamp exists.
     *
     * This keeps breakfast/lunch/dinner anchored supplements tied to actual meal
     * timing instead of silently drifting back to generic defaults.
     */
    private fun resolveAnchoredTime(
        anchor: TimeAnchor,
        offsetMinutes: Int,
        context: AnchorTimeContext,
        mealAnchorTimes: Map<TimeAnchor, LocalTime>
    ): LocalTime? {
        val baseTime = mealAnchorTimes[anchor]
            ?: resolveAnchorTimeUseCase(
                anchor = anchor,
                context = context
            )
            ?: return null

        return applyAnchorOffsetUseCase(
            baseTime = baseTime,
            offsetMinutes = offsetMinutes
        )
    }

    /**
     * Deterministic first-pass meal anchor rule:
     * - native HH meal templates are NOT concrete same-day anchor providers
     * - imported AK meals may provide anchor times
     * - if multiple imported meals map to the same anchor, the earliest time wins
     */
    private fun buildMealAnchorTimes(
        date: LocalDate,
        meals: List<Meal>,
        importedMeals: List<AkImportedMealEntity>
    ): Map<TimeAnchor, LocalTime> {
        /**
         * Native HH meals are now reusable templates without concrete timestamps.
         * Keep this path explicit for future occurrence/log-backed meal anchors.
         */
        val nativePairs = meals
            .asSequence()
            .mapNotNull { meal ->
                resolveMealAnchorUseCase(meal)
                null
            }

        val importedPairs = importedMeals
            .asSequence()
            .mapNotNull { importedMeal ->
                val importedDate = Instant
                    .fromEpochMilliseconds(importedMeal.timestamp)
                    .toLocalDateTime(DomainTimePolicy.localTimeZone)
                    .date

                if (importedDate != date) {
                    return@mapNotNull null
                }

                val anchor = importedMeal.type.toTimeAnchorOrNull()
                    ?: return@mapNotNull null

                val time = Instant
                    .fromEpochMilliseconds(importedMeal.timestamp)
                    .toLocalDateTime(DomainTimePolicy.localTimeZone)
                    .time

                anchor to time
            }

        return (nativePairs + importedPairs)
            .groupBy(
                keySelector = { it.first },
                valueTransform = { it.second }
            )
            .mapValues { (_, times) ->
                times.minByOrNull { it.toSecondOfDay() }!!
            }
    }

    private suspend fun buildAnchorTimeContext(
        date: LocalDate,
        workoutAnchors: List<WorkoutAnchorWindow>
    ): AnchorTimeContext {
        val supportedAnchors = TimeAnchor.entries

        val defaultTimes = buildMap {
            supportedAnchors.forEach { anchor ->
                val doseAnchor = anchor.toDoseAnchorTypeOrNull() ?: return@forEach
                val time = eventTimeDao
                    .getDefault(doseAnchor)
                    ?.timeSeconds
                    ?.let(LocalTime::fromSecondOfDay)
                    ?: fallbackAnchorTime(anchor)

                if (time != null) {
                    put(anchor, time)
                }
            }
        }

        val dateOverrides = buildMap {
            supportedAnchors.forEach { anchor ->
                val doseAnchor = anchor.toDoseAnchorTypeOrNull() ?: return@forEach
                val time = eventTimeDao
                    .getOverride(date.toString(), doseAnchor)
                    ?.timeSeconds
                    ?.let(LocalTime::fromSecondOfDay)

                if (time != null) {
                    put(
                        AnchorDateKey(
                            anchor = anchor,
                            date = date
                        ),
                        time
                    )
                }
            }
        }

        return AnchorTimeContext(
            date = date,
            defaultTimes = defaultTimes,
            dayOfWeekOverrides = emptyMap(),
            dateOverrides = dateOverrides,
            workoutAnchors = workoutAnchors
        )
    }

    private fun buildOccurrenceId(
        date: LocalDate,
        supplementId: Long,
        scheduleId: Long?,
        sourceRowId: Long?,
        time: LocalTime,
        sortOrder: Int
    ): String {
        return listOf(
            date.toString(),
            supplementId.toString(),
            scheduleId?.toString() ?: "ns",
            sourceRowId?.toString() ?: "nr",
            time.toSecondOfDay().toString(),
            sortOrder.toString()
        ).joinToString(separator = "|")
    }

    private fun SupplementScheduleEntity.isActiveOn(date: LocalDate): Boolean {
        if (date < startDate) return false
        if (endDate != null && date > endDate) return false
        if (interval <= 0) return false

        return when (recurrenceType) {
            ScheduleRecurrenceType.DAILY -> {
                val daysBetween = daysBetween(startDate, date)
                daysBetween >= 0 && daysBetween % interval == 0
            }

            ScheduleRecurrenceType.WEEKLY -> {
                val weeklySet = weeklyDays ?: emptyList<DayOfWeek>()
                if (!weeklySet.contains(date.dayOfWeek)) {
                    false
                } else {
                    val daysBetween = daysBetween(startDate, date)
                    val weeksBetween = daysBetween / 7
                    weeksBetween >= 0 && weeksBetween % interval == 0
                }
            }
        }
    }

    private fun daysBetween(
        start: LocalDate,
        end: LocalDate
    ): Int {
        val startEpochDay = start.toJavaLocalDate().toEpochDay()
        val endEpochDay = end.toJavaLocalDate().toEpochDay()
        return (endEpochDay - startEpochDay).toInt()
    }

    private fun MealType.toTimeAnchorOrNull(): TimeAnchor? =
        when (this) {
            MealType.BREAKFAST -> TimeAnchor.BREAKFAST
            MealType.LUNCH -> TimeAnchor.LUNCH
            MealType.DINNER -> TimeAnchor.DINNER
            MealType.SNACK -> TimeAnchor.SNACK
            MealType.PRE_WORKOUT -> TimeAnchor.BEFORE_WORKOUT
            MealType.POST_WORKOUT -> TimeAnchor.AFTER_WORKOUT
            else -> null
        }

    /**
     * Storage compatibility mapping for anchor defaults / overrides.
     *
     * Important:
     * - DURING_WORKOUT is currently aliased to BEFORE_WORKOUT for persisted
     *   default/override lookup.
     * - This preserves current storage behavior while the planner becomes
     *   deterministic on top of the existing schema.
     */
    private fun TimeAnchor.toDoseAnchorTypeOrNull(): DoseAnchorType? =
        when (this) {
            TimeAnchor.MIDNIGHT -> DoseAnchorType.MIDNIGHT
            TimeAnchor.WAKEUP -> DoseAnchorType.WAKEUP
            TimeAnchor.BREAKFAST -> DoseAnchorType.BREAKFAST
            TimeAnchor.LUNCH -> DoseAnchorType.LUNCH
            TimeAnchor.DINNER -> DoseAnchorType.DINNER
            TimeAnchor.BEFORE_WORKOUT -> DoseAnchorType.BEFORE_WORKOUT
            TimeAnchor.AFTER_WORKOUT -> DoseAnchorType.AFTER_WORKOUT
            TimeAnchor.SLEEP -> DoseAnchorType.SLEEP
            TimeAnchor.DURING_WORKOUT -> DoseAnchorType.BEFORE_WORKOUT
            TimeAnchor.SNACK -> DoseAnchorType.SNACK
        }

    private fun fallbackAnchorTime(anchor: TimeAnchor): LocalTime? =
        when (anchor) {
            TimeAnchor.MIDNIGHT -> null
            TimeAnchor.WAKEUP -> LocalTime(7, 0)
            TimeAnchor.BREAKFAST -> LocalTime(8, 0)
            TimeAnchor.LUNCH -> LocalTime(12, 0)
            TimeAnchor.DINNER -> LocalTime(18, 0)
            TimeAnchor.BEFORE_WORKOUT -> LocalTime(16, 30)
            TimeAnchor.AFTER_WORKOUT -> LocalTime(17, 45)
            TimeAnchor.SLEEP -> LocalTime(22, 0)
            TimeAnchor.DURING_WORKOUT -> LocalTime(16, 30)
            TimeAnchor.SNACK -> LocalTime(14, 30)
        }
}