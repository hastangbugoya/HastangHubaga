package com.example.hastanghubaga.domain.usecase.supplement

import com.example.hastanghubaga.data.local.dao.supplement.EventTimeDao
import com.example.hastanghubaga.data.local.entity.meal.MealType
import com.example.hastanghubaga.data.local.entity.supplement.DoseAnchorType
import com.example.hastanghubaga.data.local.entity.supplement.FrequencyType
import com.example.hastanghubaga.domain.model.supplement.DoseCondition
import com.example.hastanghubaga.domain.model.supplement.MealAwareDoseState
import com.example.hastanghubaga.domain.model.supplement.MealLog
import com.example.hastanghubaga.domain.model.supplement.ResolvedSupplementScheduleEntry
import com.example.hastanghubaga.domain.model.supplement.ResolvedSupplementTimingType
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
                .filter { it.shouldAppearOn(date) }
                .map { it.withScheduleFor(mealsToday, anchorContext) }
                .filter { it.scheduledTimes.isNotEmpty() }
        }

    private suspend fun SupplementWithUserSettings.withScheduleFor(
        mealsToday: List<MealLog>,
        anchorContext: AnchorTimeContext
    ): SupplementWithUserSettings {
        val resolvedEntries = resolveScheduleEntries(anchorContext)
        val resolvedScheduledTimes = resolvedEntries
            .map { it.time }
            .distinct()
            .sorted()

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
            resolvedScheduleEntries = resolvedEntries,
            doseState = doseState
        )
    }

    /**
     * Determines whether this supplement should be considered for the target date.
     *
     * RULES:
     * - If persisted or already-resolved schedule entries exist, the repository has already
     *   determined date applicability and we should not re-filter using legacy supplement-level
     *   recurrence fields.
     * - Persisted anchored schedule specs are already date-filtered in the repository.
     * - Legacy / fallback paths (settings-only fixed/meal-anchored or legacy supplement timing)
     *   still use supplement-level recurrence behavior for compatibility.
     */
    private fun SupplementWithUserSettings.shouldAppearOn(
        date: LocalDate
    ): Boolean {
        if (resolvedScheduleEntries.any { it.scheduleId != null }) {
            return true
        }

        if (
            scheduleSpec is SupplementScheduleSpec.Anchored ||
            scheduleSpec is SupplementScheduleSpec.AnchoredRows
        ) {
            return true
        }

        return shouldTakeOnLegacyDate(date)
    }

    private suspend fun SupplementWithUserSettings.resolveScheduleEntries(
        anchorContext: AnchorTimeContext
    ): List<ResolvedSupplementScheduleEntry> {
        val existingEntries = resolvedScheduleEntries

        val specEntries = scheduleSpec?.let { spec ->
            resolveEntriesFromScheduleSpec(
                spec = spec,
                anchorContext = anchorContext
            )
        } ?: emptyList()

        val legacyEntries =
            if (existingEntries.isEmpty() && specEntries.isEmpty()) {
                resolveEntriesFromLegacySupplementTiming(anchorContext)
            } else {
                emptyList()
            }

        return (existingEntries + specEntries + legacyEntries)
            .distinctBy { entry ->
                listOf(
                    entry.scheduleId,
                    entry.sourceRowId,
                    entry.time.toSecondOfDay(),
                    entry.timingType,
                    entry.anchor,
                    entry.label,
                    entry.sortOrder
                )
            }
            .sortedWith(
                compareBy<ResolvedSupplementScheduleEntry>({ it.time })
                    .thenBy { it.sortOrder }
                    .thenBy { it.label ?: "" }
                    .thenBy { it.scheduleId ?: Long.MAX_VALUE }
                    .thenBy { it.sourceRowId ?: Long.MAX_VALUE }
            )
    }

    private suspend fun resolveEntriesFromScheduleSpec(
        spec: SupplementScheduleSpec,
        anchorContext: AnchorTimeContext
    ): List<ResolvedSupplementScheduleEntry> {
        return when (spec) {
            is SupplementScheduleSpec.FixedTimes -> {
                spec.times
                    .distinct()
                    .sorted()
                    .mapIndexed { index, time ->
                        ResolvedSupplementScheduleEntry(
                            scheduleId = null,
                            sourceRowId = null,
                            time = time,
                            timingType = ResolvedSupplementTimingType.LEGACY,
                            anchor = null,
                            label = null,
                            sortOrder = index
                        )
                    }
            }

            is SupplementScheduleSpec.MealAnchored -> {
                spec.mealTypes
                    .mapNotNull { mealType ->
                        mealType.toTimeAnchorOrNull()
                    }
                    .mapIndexedNotNull { index, anchor ->
                        resolveAnchoredEntry(
                            scheduleId = null,
                            sourceRowId = null,
                            anchor = anchor,
                            offsetMinutes = spec.offsetMinutes,
                            label = null,
                            sortOrder = index,
                            context = anchorContext
                        )
                    }
                    .sortedWith(
                        compareBy<ResolvedSupplementScheduleEntry>({ it.time })
                            .thenBy { it.sortOrder }
                    )
            }

            is SupplementScheduleSpec.Anchored -> {
                spec.anchors
                    .toList()
                    .sortedBy { it.name }
                    .mapIndexedNotNull { index, anchor ->
                        resolveAnchoredEntry(
                            scheduleId = null,
                            sourceRowId = null,
                            anchor = anchor,
                            offsetMinutes = spec.offsetMinutes,
                            label = null,
                            sortOrder = index,
                            context = anchorContext
                        )
                    }
                    .sortedWith(
                        compareBy<ResolvedSupplementScheduleEntry>({ it.time })
                            .thenBy { it.sortOrder }
                    )
            }

            is SupplementScheduleSpec.AnchoredRows -> {
                spec.rows
                    .sortedWith(
                        compareBy(
                            { it.sortOrder },
                            { it.anchor.name },
                            { it.label ?: "" }
                        )
                    )
                    .mapIndexedNotNull { index, row ->
                        resolveAnchoredEntry(
                            scheduleId = null,
                            sourceRowId = null,
                            anchor = row.anchor,
                            offsetMinutes = row.offsetMinutes,
                            label = row.label,
                            sortOrder = row.sortOrder.takeIf { it != 0 } ?: index,
                            context = anchorContext
                        )
                    }
                    .sortedWith(
                        compareBy<ResolvedSupplementScheduleEntry>({ it.time })
                            .thenBy { it.sortOrder }
                            .thenBy { it.label ?: "" }
                    )
            }
        }
    }

    private suspend fun SupplementWithUserSettings.resolveEntriesFromLegacySupplementTiming(
        anchorContext: AnchorTimeContext
    ): List<ResolvedSupplementScheduleEntry> {
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
            val resolvedTime = applyAnchorOffsetUseCase(
                baseTime = baseTime,
                offsetMinutes = index * offsetMinutes
            )

            ResolvedSupplementScheduleEntry(
                scheduleId = null,
                sourceRowId = null,
                time = resolvedTime,
                timingType = ResolvedSupplementTimingType.LEGACY,
                anchor = anchor,
                label = null,
                sortOrder = index
            )
        }.sortedWith(
            compareBy<ResolvedSupplementScheduleEntry>({ it.time })
                .thenBy { it.sortOrder }
        )
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

    private fun resolveAnchoredEntry(
        scheduleId: Long?,
        sourceRowId: Long?,
        anchor: TimeAnchor,
        offsetMinutes: Int,
        label: String?,
        sortOrder: Int,
        context: AnchorTimeContext
    ): ResolvedSupplementScheduleEntry? {
        val time = resolveAnchoredTime(
            anchor = anchor,
            offsetMinutes = offsetMinutes,
            context = context
        ) ?: return null

        return ResolvedSupplementScheduleEntry(
            scheduleId = scheduleId,
            sourceRowId = sourceRowId,
            time = time,
            timingType = ResolvedSupplementTimingType.ANCHORED,
            anchor = anchor,
            label = label,
            sortOrder = sortOrder
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

    /**
     * Legacy supplement-level recurrence compatibility path.
     *
     * This remains important for old supplement timing flows and for the future
     * "strict every N days" interpretation that may depend on actual last-taken data.
     *
     * Persisted schedule rows should eventually be evaluated independently from these
     * supplement-level fields.
     */
    private fun SupplementWithUserSettings.shouldTakeOnLegacyDate(
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