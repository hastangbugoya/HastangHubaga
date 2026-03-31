package com.example.hastanghubaga.domain.usecase.supplement

import com.example.hastanghubaga.data.local.dao.supplement.EventTimeDao
import com.example.hastanghubaga.data.local.entity.meal.MealType
import com.example.hastanghubaga.data.local.entity.supplement.DoseAnchorType
import com.example.hastanghubaga.data.local.entity.supplement.FrequencyType
import com.example.hastanghubaga.domain.model.supplement.DoseCondition
import com.example.hastanghubaga.domain.model.supplement.MealAwareDoseState
import com.example.hastanghubaga.domain.model.supplement.MealLog
import com.example.hastanghubaga.domain.model.supplement.SupplementScheduleSpec
import com.example.hastanghubaga.domain.model.supplement.SupplementWithUserSettings
import com.example.hastanghubaga.domain.repository.activity.ActivityRepository
import com.example.hastanghubaga.domain.repository.supplement.SupplementRepository
import com.example.hastanghubaga.domain.schedule.model.AnchorDateKey
import com.example.hastanghubaga.domain.schedule.model.AnchorTimeContext
import com.example.hastanghubaga.domain.schedule.model.TimeAnchor
import com.example.hastanghubaga.domain.schedule.model.WorkoutAnchorWindow
import com.example.hastanghubaga.domain.schedule.timing.ApplyAnchorOffsetUseCase
import com.example.hastanghubaga.domain.schedule.timing.ResolveAnchorTimeUseCase
import com.example.hastanghubaga.domain.time.DomainTimePolicy
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant

class GetSupplementsWithUserSettingsForDateUseCase @Inject constructor(
    private val supplementRepository: SupplementRepository,
    private val activityRepository: ActivityRepository,
    private val eventTimeDao: EventTimeDao,
    private val resolveAnchorTimeUseCase: ResolveAnchorTimeUseCase,
    private val applyAnchorOffsetUseCase: ApplyAnchorOffsetUseCase,
    private val clock: Clock = Clock.System
) {

    operator fun invoke(
        date: LocalDate
    ): Flow<List<SupplementWithUserSettings>> =
        combine(
            supplementRepository.getSupplementsForDate(date.toString()),
            activityRepository.observeActivitiesForDate(date)
        ) { supplements, activities ->
            val mealsToday: List<MealLog> = emptyList()
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

            val anchorContext = buildAnchorTimeContext(
                date = date,
                workoutAnchors = workoutAnchors
            )

            supplements
                .filter { it.shouldTakeOn(date) }
                .map { it.withScheduleFor(mealsToday, anchorContext) }
        }

    private suspend fun SupplementWithUserSettings.withScheduleFor(
        mealsToday: List<MealLog>,
        anchorContext: AnchorTimeContext
    ): SupplementWithUserSettings {
        val resolvedScheduledTimes = resolveScheduledTimes(anchorContext)

        val doseState =
            resolvedScheduledTimes.firstOrNull()?.let { time ->
                resolveDoseState(
                    scheduledTime = time,
                    doseConditions = supplement.doseConditions,
                    mealsToday = mealsToday
                )
            } ?: MealAwareDoseState.Ready

        return copy(
            scheduledTimes = resolvedScheduledTimes,
            doseState = doseState
        )
    }

    private suspend fun SupplementWithUserSettings.resolveScheduledTimes(
        anchorContext: AnchorTimeContext
    ): List<LocalTime> {
        if (scheduleSpec is SupplementScheduleSpec.FixedTimes && scheduledTimes.isNotEmpty()) {
            return scheduledTimes.sorted()
        }

        val specTimes = scheduleSpec?.let { spec ->
            resolveFromScheduleSpec(
                spec = spec,
                anchorContext = anchorContext
            )
        }

        if (specTimes != null) {
            return specTimes
        }

        return resolveFromLegacySupplementTiming(anchorContext)
    }

    private suspend fun resolveFromScheduleSpec(
        spec: SupplementScheduleSpec,
        anchorContext: AnchorTimeContext
    ): List<LocalTime> {
        return when (spec) {
            is SupplementScheduleSpec.FixedTimes -> {
                spec.times.sorted()
            }

            is SupplementScheduleSpec.MealAnchored -> {
                spec.mealTypes
                    .mapNotNull { mealType ->
                        mealType.toTimeAnchorOrNull()
                    }
                    .mapNotNull { anchor ->
                        resolveAnchoredTime(
                            anchor = anchor,
                            offsetMinutes = spec.offsetMinutes,
                            context = anchorContext
                        )
                    }
                    .sorted()
            }

            is SupplementScheduleSpec.Anchored -> {
                spec.anchors
                    .mapNotNull { anchor ->
                        resolveAnchoredTime(
                            anchor = anchor,
                            offsetMinutes = spec.offsetMinutes,
                            context = anchorContext
                        )
                    }
                    .distinct()
                    .sorted()
            }
        }
    }

    private suspend fun SupplementWithUserSettings.resolveFromLegacySupplementTiming(
        anchorContext: AnchorTimeContext
    ): List<LocalTime> {
        val dosesPerDay = resolveDosesPerDay()
        val offsetMinutes = supplement.offsetMinutes ?: 0
        val anchor = supplement.doseAnchorType.toLegacyFallbackTimeAnchor() ?: return emptyList()

        val baseTime = resolveAnchoredTime(
            anchor = anchor,
            offsetMinutes = 0,
            context = anchorContext
        ) ?: return emptyList()

        val doseCount = kotlin.math.ceil(dosesPerDay).toInt()

        return List(doseCount) { index ->
            applyAnchorOffsetUseCase(
                baseTime = baseTime,
                offsetMinutes = index * offsetMinutes
            )
        }.sorted()
    }

    private fun SupplementWithUserSettings.resolveDosesPerDay(): Double =
        userSettings?.preferredServingsPerDay
            ?: supplement.servingsPerDay

    private suspend fun buildAnchorTimeContext(
        date: LocalDate,
        workoutAnchors: List<WorkoutAnchorWindow>
    ): AnchorTimeContext {
        val supportedAnchors = listOf(
            TimeAnchor.MIDNIGHT,
            TimeAnchor.WAKEUP,
            TimeAnchor.BREAKFAST,
            TimeAnchor.LUNCH,
            TimeAnchor.DINNER,
            TimeAnchor.BEFORE_WORKOUT,
            TimeAnchor.DURING_WORKOUT,
            TimeAnchor.AFTER_WORKOUT,
            TimeAnchor.SLEEP
        )

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

    private fun resolveAnchoredTime(
        anchor: TimeAnchor,
        offsetMinutes: Int,
        context: AnchorTimeContext
    ): LocalTime? {
        val baseTime = resolveAnchorTimeUseCase(
            anchor = anchor,
            context = context
        ) ?: return null

        return applyAnchorOffsetUseCase(
            baseTime = baseTime,
            offsetMinutes = offsetMinutes
        )
    }

    private fun SupplementWithUserSettings.shouldTakeOn(
        date: LocalDate
    ): Boolean =
        when (supplement.frequencyType) {
            FrequencyType.DAILY -> true
            FrequencyType.WEEKLY ->
                supplement.weeklyDays?.contains(date.dayOfWeek) == true

            FrequencyType.EVERY_X_DAYS ->
                shouldTakeEveryXDays(date)
        }

    private fun SupplementWithUserSettings.shouldTakeEveryXDays(
        date: LocalDate
    ): Boolean {
        val intervalDays = supplement.frequencyInterval ?: return false
        val lastTaken = supplement.lastTakenDate
            ?.let(LocalDate::parse)
            ?: return true

        val nextEligibleDate =
            lastTaken.plus(intervalDays, DateTimeUnit.DAY)

        return date >= nextEligibleDate
    }

    private fun MealType.toTimeAnchorOrNull(): TimeAnchor? =
        when (this) {
            MealType.BREAKFAST -> TimeAnchor.BREAKFAST
            MealType.LUNCH -> TimeAnchor.LUNCH
            MealType.DINNER -> TimeAnchor.DINNER
            else -> null
        }

    private fun DoseAnchorType.toLegacyFallbackTimeAnchor(): TimeAnchor? =
        when (this) {
            DoseAnchorType.MIDNIGHT -> TimeAnchor.WAKEUP
            else -> toTimeAnchorOrNull()
        }

    private fun DoseAnchorType.toTimeAnchorOrNull(): TimeAnchor? =
        when (this) {
            DoseAnchorType.MIDNIGHT -> TimeAnchor.MIDNIGHT
            DoseAnchorType.WAKEUP -> TimeAnchor.WAKEUP
            DoseAnchorType.BREAKFAST -> TimeAnchor.BREAKFAST
            DoseAnchorType.LUNCH -> TimeAnchor.LUNCH
            DoseAnchorType.DINNER -> TimeAnchor.DINNER
            DoseAnchorType.BEFORE_WORKOUT -> TimeAnchor.BEFORE_WORKOUT
            DoseAnchorType.AFTER_WORKOUT -> TimeAnchor.AFTER_WORKOUT
            DoseAnchorType.SLEEP -> TimeAnchor.SLEEP
            DoseAnchorType.ANYTIME -> null
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
        }

    fun resolveDoseState(
        scheduledTime: LocalTime,
        doseConditions: Set<DoseCondition>,
        mealsToday: List<MealLog>
    ): MealAwareDoseState {
        val lastMealTime = mealsToday
            .maxByOrNull { it.time }
            ?.time

        if (DoseCondition.EMPTY_STOMACH in doseConditions) {
            val date = DomainTimePolicy.todayLocal(clock)
            val minutesSinceLastMeal =
                lastMealTime?.let { lastMeal ->
                    val mealDate =
                        if (lastMeal > scheduledTime) {
                            date.minus(1, DateTimeUnit.DAY)
                        } else {
                            date
                        }

                    val mealDateTime = LocalDateTime(mealDate, lastMeal)
                    val scheduledDateTime = LocalDateTime(date, scheduledTime)

                    scheduledDateTime
                        .toInstant(TimeZone.currentSystemDefault())
                        .minus(
                            mealDateTime.toInstant(TimeZone.currentSystemDefault())
                        )
                        .inWholeMinutes
                }

            val isEmptyStomach =
                lastMealTime == null || (minutesSinceLastMeal ?: 0) >= 120

            if (!isEmptyStomach) {
                return MealAwareDoseState.PendingEmptyStomach(
                    reason = "Best taken on an empty stomach. Consider waiting before your next meal."
                )
            }
        }

        if (DoseCondition.WITH_FOOD in doseConditions) {
            val minutesBetween =
                lastMealTime?.let { mealTime ->
                    (scheduledTime.toSecondOfDay() - mealTime.toSecondOfDay()) / 60
                }

            val hasRecentMeal =
                minutesBetween != null && minutesBetween <= 60

            if (!hasRecentMeal) {
                return MealAwareDoseState.PendingMeal(
                    reason = "Best taken with food to improve absorption or reduce stomach discomfort."
                )
            }
        }

        if (DoseCondition.AVOID_CAFFEINE in doseConditions) {
            return MealAwareDoseState.Advisory(
                reason = "Avoid caffeine close to this dose if possible."
            )
        }

        return MealAwareDoseState.Ready
    }
}