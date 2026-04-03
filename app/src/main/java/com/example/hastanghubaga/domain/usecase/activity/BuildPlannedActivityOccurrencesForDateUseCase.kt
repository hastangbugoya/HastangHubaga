package com.example.hastanghubaga.domain.usecase.activity

import com.example.hastanghubaga.data.local.dao.activity.ActivityEntityDao
import com.example.hastanghubaga.data.local.dao.activity.ActivityScheduleDao
import com.example.hastanghubaga.data.local.dao.supplement.EventTimeDao
import com.example.hastanghubaga.data.local.entity.activity.ActivityOccurrenceEntity
import com.example.hastanghubaga.data.local.entity.activity.ActivityOccurrenceSourceType
import com.example.hastanghubaga.data.local.entity.activity.ActivityScheduleAnchoredTimeEntity
import com.example.hastanghubaga.data.local.entity.activity.ActivityScheduleEntity
import com.example.hastanghubaga.data.local.entity.activity.ActivityScheduleFixedTimeEntity
import com.example.hastanghubaga.data.local.entity.meal.AkImportedMealEntity
import com.example.hastanghubaga.data.local.entity.meal.MealType
import com.example.hastanghubaga.data.local.entity.supplement.DoseAnchorType
import com.example.hastanghubaga.data.local.entity.supplement.ScheduleRecurrenceType
import com.example.hastanghubaga.data.local.entity.supplement.ScheduleTimingType
import com.example.hastanghubaga.domain.model.meal.Meal
import com.example.hastanghubaga.domain.repository.activity.ActivityRepository
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
 * Builds the deterministic PLANNED activity occurrence set for a single date.
 *
 * Canonical rules:
 * - Only persisted activity schedules are considered authoritative here
 * - Legacy inline activity timing / alert fields are intentionally ignored
 * - Inactive activity templates are excluded from planning
 * - One fixed-time child row => one planned occurrence
 * - One anchored child row that resolves successfully => one planned occurrence
 * - occurrenceId is deterministic and stable for the same:
 *   date + activityId + scheduleId + sourceRowId + resolved time + sortOrder
 *
 * Anchor resolution rules:
 * - Meal anchors use concrete same-day meal timestamps when available
 * - Workout anchors currently use same-day actual activities observable for the date
 * - If a concrete meal/workout time is unavailable, resolution falls back to:
 *   date override -> day-of-week override -> default anchor time
 *
 * Workout flag rules:
 * - ActivityEntity.isWorkout is treated as the default/template truth
 * - Planned occurrences snapshot that default into ActivityOccurrenceEntity.isWorkout
 * - Per-occurrence user toggles can later override the planned snapshot without
 *   mutating the underlying template
 *
 * Important current limitation:
 * - This first-pass planner does not yet use planned activity occurrences as
 *   anchor providers during the same build pass.
 * - That keeps behavior aligned with the current supplement planner shape.
 */
class BuildPlannedActivityOccurrencesForDateUseCase @Inject constructor(
    private val activityEntityDao: ActivityEntityDao,
    private val activityScheduleDao: ActivityScheduleDao,
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
    ): List<ActivityOccurrenceEntity> {
        val activityDefinitions = activityEntityDao
            .getAllActivities()
            .filter { it.isActive }

        val actualActivities = activityRepository.observeActivitiesForDate(date).first()

        val workoutAnchors = actualActivities
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

        return activityDefinitions
            .flatMap { activity ->
                val schedules = activityScheduleDao
                    .getSchedulesForActivity(activity.id)
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
                                activityId = activity.id,
                                isWorkout = activity.isWorkout,
                                schedule = schedule
                            )

                        ScheduleTimingType.ANCHORED ->
                            buildAnchoredOccurrences(
                                date = date,
                                activityId = activity.id,
                                isWorkout = activity.isWorkout,
                                schedule = schedule,
                                anchorContext = anchorContext,
                                mealAnchorTimes = mealAnchorTimes
                            )
                    }
                }
            }
            .distinctBy { it.id }
            .sortedWith(
                compareBy<ActivityOccurrenceEntity>({ it.plannedTimeSeconds })
                    .thenBy { it.id }
            )
    }

    private suspend fun buildFixedOccurrences(
        date: LocalDate,
        activityId: Long,
        isWorkout: Boolean,
        schedule: ActivityScheduleEntity
    ): List<ActivityOccurrenceEntity> {
        val rows = activityScheduleDao
            .getFixedTimesForSchedule(schedule.id)
            .sortedWith(
                compareBy<ActivityScheduleFixedTimeEntity>(
                    { it.sortOrder },
                    { it.time.hour },
                    { it.time.minute },
                    { it.id }
                )
            )

        return rows.map { row ->
            ActivityOccurrenceEntity(
                id = buildOccurrenceId(
                    date = date,
                    activityId = activityId,
                    scheduleId = schedule.id,
                    sourceRowId = row.id,
                    time = row.time,
                    sortOrder = row.sortOrder
                ),
                activityId = activityId,
                scheduleId = schedule.id,
                date = date.toString(),
                plannedTimeSeconds = row.time.toSecondOfDay(),
                sourceType = ActivityOccurrenceSourceType.SCHEDULED,
                isDeleted = false,
                isWorkout = isWorkout
            )
        }
    }

    private suspend fun buildAnchoredOccurrences(
        date: LocalDate,
        activityId: Long,
        isWorkout: Boolean,
        schedule: ActivityScheduleEntity,
        anchorContext: AnchorTimeContext,
        mealAnchorTimes: Map<TimeAnchor, LocalTime>
    ): List<ActivityOccurrenceEntity> {
        val rows = activityScheduleDao
            .getAnchoredTimesForSchedule(schedule.id)
            .sortedWith(
                compareBy<ActivityScheduleAnchoredTimeEntity>(
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

            ActivityOccurrenceEntity(
                id = buildOccurrenceId(
                    date = date,
                    activityId = activityId,
                    scheduleId = schedule.id,
                    sourceRowId = row.id,
                    time = resolvedTime,
                    sortOrder = row.sortOrder
                ),
                activityId = activityId,
                scheduleId = schedule.id,
                date = date.toString(),
                plannedTimeSeconds = resolvedTime.toSecondOfDay(),
                sourceType = ActivityOccurrenceSourceType.SCHEDULED,
                isDeleted = false,
                isWorkout = isWorkout
            )
        }
    }

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

    private fun buildMealAnchorTimes(
        date: LocalDate,
        meals: List<Meal>,
        importedMeals: List<AkImportedMealEntity>
    ): Map<TimeAnchor, LocalTime> {
        val nativePairs = meals
            .asSequence()
            .filter { it.timestamp.date == date }
            .mapNotNull { meal ->
                val anchor = resolveMealAnchorUseCase(meal) ?: return@mapNotNull null
                anchor to meal.timestamp.time
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
        activityId: Long,
        scheduleId: Long?,
        sourceRowId: Long?,
        time: LocalTime,
        sortOrder: Int
    ): String {
        return listOf(
            date.toString(),
            activityId.toString(),
            scheduleId?.toString() ?: "ns",
            sourceRowId?.toString() ?: "nr",
            time.toSecondOfDay().toString(),
            sortOrder.toString()
        ).joinToString(separator = "|")
    }

    private fun ActivityScheduleEntity.isActiveOn(date: LocalDate): Boolean {
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