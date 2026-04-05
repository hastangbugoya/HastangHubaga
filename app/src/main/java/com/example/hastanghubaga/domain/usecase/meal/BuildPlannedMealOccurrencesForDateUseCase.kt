package com.example.hastanghubaga.domain.usecase.meal

import com.example.hastanghubaga.data.local.dao.meal.MealEntityDao
import com.example.hastanghubaga.data.local.dao.meal.MealScheduleDao
import com.example.hastanghubaga.data.local.dao.supplement.EventTimeDao
import com.example.hastanghubaga.data.local.entity.meal.MealOccurrenceEntity
import com.example.hastanghubaga.data.local.entity.meal.MealOccurrenceSourceType
import com.example.hastanghubaga.data.local.entity.meal.MealScheduleAnchoredTimeEntity
import com.example.hastanghubaga.data.local.entity.meal.MealScheduleEntity
import com.example.hastanghubaga.data.local.entity.meal.MealScheduleFixedTimeEntity
import com.example.hastanghubaga.data.local.entity.meal.MealType
import com.example.hastanghubaga.data.local.entity.supplement.DoseAnchorType
import com.example.hastanghubaga.domain.schedule.model.AnchorDateKey
import com.example.hastanghubaga.domain.schedule.model.AnchorTimeContext
import com.example.hastanghubaga.domain.schedule.model.TimeAnchor
import com.example.hastanghubaga.domain.schedule.timing.ApplyAnchorOffsetUseCase
import com.example.hastanghubaga.domain.schedule.timing.ResolveAnchorTimeUseCase
import javax.inject.Inject
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.toJavaLocalDate

/**
 * Builds the deterministic PLANNED meal occurrence set for a single date.
 *
 * Canonical rules:
 * - Only persisted meal schedules are authoritative here
 * - MealEntity is a reusable template, not a timestamped event row
 * - Inactive meal templates are excluded from planning
 * - One fixed-time child row => one planned occurrence
 * - One anchored child row that resolves successfully => one planned occurrence
 * - occurrenceId is deterministic and stable for the same:
 *   date + mealId + scheduleId + sourceRowId + resolved time + sortOrder
 *
 * Anchor resolution rules:
 * - Meal schedules may anchor to default/day override anchor times
 * - This first pass does NOT attempt same-pass chaining against other newly
 *   planned meals. That keeps behavior deterministic and aligned with the
 *   current activity occurrence builder strategy.
 *
 * Timeline intent:
 * - planned meal timeline rows should later come from MealOccurrenceEntity
 * - future meal logs should reconcile against these via occurrenceId
 */
class BuildPlannedMealOccurrencesForDateUseCase @Inject constructor(
    private val mealEntityDao: MealEntityDao,
    private val mealScheduleDao: MealScheduleDao,
    private val eventTimeDao: EventTimeDao,
    private val resolveAnchorTimeUseCase: ResolveAnchorTimeUseCase,
    private val applyAnchorOffsetUseCase: ApplyAnchorOffsetUseCase
) {

    suspend operator fun invoke(
        date: LocalDate
    ): List<MealOccurrenceEntity> {
        val mealDefinitions = mealEntityDao
            .getAllMealsOnce()
            .filter { it.meal.isActive }

        val anchorContext = buildAnchorTimeContext(date)

        return mealDefinitions
            .flatMap { joined ->
                val meal = joined.meal

                val schedules = mealScheduleDao
                    .getSchedulesForMeal(meal.id)
                    .asSequence()
                    .filter { it.isEnabled }
                    .filter { it.isActiveOn(date) }
                    .sortedBy { it.id }
                    .toList()

                schedules.flatMap { schedule ->
                    when (schedule.timingType) {
                        "FIXED_TIMES" ->
                            buildFixedOccurrences(
                                date = date,
                                mealId = meal.id,
                                schedule = schedule
                            )

                        "ANCHORED" ->
                            buildAnchoredOccurrences(
                                date = date,
                                mealId = meal.id,
                                schedule = schedule,
                                anchorContext = anchorContext
                            )

                        else -> emptyList()
                    }
                }
            }
            .distinctBy { it.id }
            .sortedWith(
                compareBy<MealOccurrenceEntity>({ it.plannedTimeSeconds })
                    .thenBy { it.id }
            )
    }

    private suspend fun buildFixedOccurrences(
        date: LocalDate,
        mealId: Long,
        schedule: MealScheduleEntity
    ): List<MealOccurrenceEntity> {
        val rows = mealScheduleDao
            .getFixedTimesForSchedule(schedule.id)
            .sortedWith(
                compareBy<MealScheduleFixedTimeEntity>(
                    { parseStoredTime(it.time).toSecondOfDay() },
                    { it.id }
                )
            )

        return rows.map { row ->
            val time = parseStoredTime(row.time)

            MealOccurrenceEntity(
                id = buildOccurrenceId(
                    date = date,
                    mealId = mealId,
                    scheduleId = schedule.id,
                    sourceRowId = row.id,
                    time = time,
                    sortOrder = 0
                ),
                mealId = mealId,
                scheduleId = schedule.id,
                date = date.toString(),
                plannedTimeSeconds = time.toSecondOfDay(),
                sourceType = MealOccurrenceSourceType.SCHEDULED,
                isDeleted = false
            )
        }
    }

    private suspend fun buildAnchoredOccurrences(
        date: LocalDate,
        mealId: Long,
        schedule: MealScheduleEntity,
        anchorContext: AnchorTimeContext
    ): List<MealOccurrenceEntity> {
        val rows = mealScheduleDao
            .getAnchoredTimesForSchedule(schedule.id)
            .sortedWith(compareBy<MealScheduleAnchoredTimeEntity> { it.id })

        return rows.mapNotNull { row ->
            val anchor = row.anchorType.toTimeAnchor()
            val baseTime = resolveAnchorTimeUseCase(
                anchor = anchor,
                context = anchorContext
            ) ?: return@mapNotNull null

            val resolvedTime = applyAnchorOffsetUseCase(
                baseTime = baseTime,
                offsetMinutes = row.offsetMinutes
            )

            MealOccurrenceEntity(
                id = buildOccurrenceId(
                    date = date,
                    mealId = mealId,
                    scheduleId = schedule.id,
                    sourceRowId = row.id,
                    time = resolvedTime,
                    sortOrder = 0
                ),
                mealId = mealId,
                scheduleId = schedule.id,
                date = date.toString(),
                plannedTimeSeconds = resolvedTime.toSecondOfDay(),
                sourceType = MealOccurrenceSourceType.SCHEDULED,
                isDeleted = false
            )
        }
    }

    private suspend fun buildAnchorTimeContext(
        date: LocalDate
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
            workoutAnchors = emptyList()
        )
    }

    private fun buildOccurrenceId(
        date: LocalDate,
        mealId: Long,
        scheduleId: Long?,
        sourceRowId: Long?,
        time: LocalTime,
        sortOrder: Int
    ): String {
        return listOf(
            date.toString(),
            mealId.toString(),
            scheduleId?.toString() ?: "ns",
            sourceRowId?.toString() ?: "nr",
            time.toSecondOfDay().toString(),
            sortOrder.toString()
        ).joinToString(separator = "|")
    }

    private fun MealScheduleEntity.isActiveOn(date: LocalDate): Boolean {
        val start = LocalDate.parse(startDate)
        val end = endDate?.let(LocalDate::parse)

        if (date < start) return false
        if (end != null && date > end) return false
        if (interval <= 0) return false

        return when (recurrenceType) {
            "DAILY" -> {
                val daysBetween = daysBetween(start, date)
                daysBetween >= 0 && daysBetween % interval == 0
            }

            "WEEKLY" -> {
                val weeklySet = weeklyDays.toDayOfWeekSet()
                if (!weeklySet.contains(date.dayOfWeek)) {
                    false
                } else {
                    val daysBetween = daysBetween(start, date)
                    val weeksBetween = daysBetween / 7
                    weeksBetween >= 0 && weeksBetween % interval == 0
                }
            }

            else -> false
        }
    }

    private fun String?.toDayOfWeekSet(): Set<DayOfWeek> {
        if (this.isNullOrBlank()) return emptySet()

        return split(",")
            .mapNotNull { raw ->
                runCatching { DayOfWeek.valueOf(raw.trim()) }.getOrNull()
            }
            .toSet()
    }

    private fun daysBetween(
        start: LocalDate,
        end: LocalDate
    ): Int {
        val startEpochDay = start.toJavaLocalDate().toEpochDay()
        val endEpochDay = end.toJavaLocalDate().toEpochDay()
        return (endEpochDay - startEpochDay).toInt()
    }

    private fun String.toTimeAnchor(): TimeAnchor =
        when (this) {
            MealType.BREAKFAST.name -> TimeAnchor.BREAKFAST
            MealType.LUNCH.name -> TimeAnchor.LUNCH
            MealType.DINNER.name -> TimeAnchor.DINNER
            MealType.SNACK.name -> TimeAnchor.SNACK
            MealType.PRE_WORKOUT.name -> TimeAnchor.BEFORE_WORKOUT
            MealType.POST_WORKOUT.name -> TimeAnchor.AFTER_WORKOUT
            "WAKEUP" -> TimeAnchor.WAKEUP
            "SLEEP" -> TimeAnchor.SLEEP
            "MIDNIGHT" -> TimeAnchor.MIDNIGHT
            else -> TimeAnchor.BREAKFAST
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

    private fun parseStoredTime(value: String): LocalTime {
        val parts = value.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 0
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
        val second = parts.getOrNull(2)?.toIntOrNull() ?: 0
        return LocalTime(hour, minute, second)
    }
}