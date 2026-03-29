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
import com.example.hastanghubaga.domain.repository.supplement.SupplementRepository
import com.example.hastanghubaga.domain.schedule.model.AnchorTimeContext
import com.example.hastanghubaga.domain.schedule.model.TimeAnchor
import com.example.hastanghubaga.domain.schedule.timing.ApplyAnchorOffsetUseCase
import com.example.hastanghubaga.domain.schedule.timing.ResolveAnchorTimeUseCase
import com.example.hastanghubaga.domain.time.DomainTimePolicy
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import javax.inject.Inject

/**
 * Returns supplements for a specific date, enriched with resolved daily schedule times.
 *
 * Scheduling source of truth:
 * - Prefer [SupplementWithUserSettings.scheduleSpec] when present.
 * - Populate [SupplementWithUserSettings.scheduledTimes] as a compatibility output for
 *   existing timeline/UI code that still expects concrete times.
 *
 * Legacy supplement timing fields:
 * - Fields on [com.example.hastanghubaga.domain.model.supplement.Supplement] such as
 *   `doseAnchorType`, `offsetMinutes`, `frequencyType`, `frequencyInterval`, and
 *   `weeklyDays` are now treated as legacy recommendation / dosage-guidance metadata.
 * - They are still used here as a compatibility fallback while the app transitions toward
 *   explicit schedule models, but they should no longer be considered the preferred or
 *   authoritative scheduling source for new code.
 *
 * Current scope:
 * - No recurrence redesign yet
 * - No timeline behavior changes
 * - No persistence changes
 *
 * This use case remains the bridge between schedule intent and concrete "today" times.
 */
class GetSupplementsWithUserSettingsForDateUseCase @Inject constructor(
    private val supplementRepository: SupplementRepository,
    private val eventTimeDao: EventTimeDao,
    private val resolveAnchorTimeUseCase: ResolveAnchorTimeUseCase,
    private val applyAnchorOffsetUseCase: ApplyAnchorOffsetUseCase,
    private val clock: Clock = Clock.System
) {

    /**
     * Resolves supplements for the provided date.
     *
     * Important:
     * - [scheduledTimes] is still emitted for backward compatibility.
     * - New scheduling behavior should prefer schedule intent models and pure resolvers,
     *   while legacy supplement timing fields remain fallback-only.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(
        date: LocalDate
    ): Flow<List<SupplementWithUserSettings>> =
        supplementRepository
            .getSupplementsForDate(date.toString())
            .mapLatest { supplements ->
                val mealsToday: List<MealLog> = emptyList()
                val anchorContext = buildAnchorTimeContext(date)

                supplements
                    .filter { it.shouldTakeOn(date) }
                    .map { it.withScheduleFor(mealsToday, anchorContext) }
            }

    // ---------------------------------------------------------------------
    // Scheduling
    // ---------------------------------------------------------------------

    /**
     * Resolves today's concrete schedule times for a supplement.
     *
     * Resolution order:
     * 1. Explicit [SupplementWithUserSettings.scheduleSpec]
     * 2. Legacy supplement timing fields as compatibility fallback
     *
     * This preserves existing callers that depend on [SupplementWithUserSettings.scheduledTimes]
     * while gradually moving scheduling intent out of the legacy supplement model.
     */
    private suspend fun SupplementWithUserSettings.withScheduleFor(
        mealsToday: List<MealLog>,
        anchorContext: AnchorTimeContext
    ): SupplementWithUserSettings {
        val scheduledTimes = resolveScheduledTimes(anchorContext)

        val doseState =
            scheduledTimes.firstOrNull()?.let { time ->
                resolveDoseState(
                    scheduledTime = time,
                    doseConditions = supplement.doseConditions,
                    mealsToday = mealsToday
                )
            } ?: MealAwareDoseState.Ready

        return copy(
            scheduledTimes = scheduledTimes,
            doseState = doseState
        )
    }

    /**
     * Resolves concrete times for a supplement on a date.
     *
     * Preferred path:
     * - [scheduleSpec]
     *
     * Compatibility path:
     * - legacy timing fields on [supplement]
     */
    private suspend fun SupplementWithUserSettings.resolveScheduledTimes(
        anchorContext: AnchorTimeContext
    ): List<LocalTime> {
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

    /**
     * Resolves the newer supplement schedule spec into concrete times.
     *
     * Notes:
     * - Fixed times remain straightforward clock times.
     * - Meal-anchored times are mapped into the shared anchor system.
     * - Unsupported meal types are dropped rather than guessed.
     */
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
        }
    }

    /**
     * Compatibility fallback for legacy supplement timing fields.
     *
     * These fields are now interpreted as dosage guidance / legacy schedule hints,
     * not the preferred scheduling source for new code.
     *
     * Important legacy behavior:
     * - MIDNIGHT is treated as an unset/sentinel value and falls back to WAKEUP
     *   so older supplement rows still surface on the timeline.
     *
     * Multi-dose behavior remains unchanged:
     * - resolve the legacy anchor time
     * - repeat doses using `index * offsetMinutes`
     */
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

    /**
     * Builds the anchor context used by shared anchor resolvers.
     *
     * Current backing storage only provides:
     * - per-date overrides
     * - global defaults
     *
     * Day-of-week overrides are not wired for supplements yet, so they remain empty here.
     */
    private suspend fun buildAnchorTimeContext(
        date: LocalDate
    ): AnchorTimeContext {
        val supportedAnchors = listOf(
            TimeAnchor.MIDNIGHT,
            TimeAnchor.WAKEUP,
            TimeAnchor.BREAKFAST,
            TimeAnchor.LUNCH,
            TimeAnchor.DINNER,
            TimeAnchor.BEFORE_WORKOUT,
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
                        com.example.hastanghubaga.domain.schedule.model.AnchorDateKey(
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
            dateOverrides = dateOverrides
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

    // ---------------------------------------------------------------------
    // Frequency rules
    // ---------------------------------------------------------------------

    /**
     * Date eligibility still uses legacy supplement frequency fields for now.
     *
     * This is an intentional compatibility bridge while recurrence remains out of scope.
     * These fields should be treated as legacy schedule guidance rather than the preferred
     * scheduling source for future work.
     */
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

    // ---------------------------------------------------------------------
    // Anchor mapping / defaults
    // ---------------------------------------------------------------------

    private fun MealType.toTimeAnchorOrNull(): TimeAnchor? =
        when (this) {
            MealType.BREAKFAST -> TimeAnchor.BREAKFAST
            MealType.LUNCH -> TimeAnchor.LUNCH
            MealType.DINNER -> TimeAnchor.DINNER
            else -> null
        }

    /**
     * General anchor mapping used for explicit schedule-spec based resolution.
     */
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

    /**
     * Legacy supplement fallback mapping.
     *
     * MIDNIGHT historically meant "unset / no user-selected event time" rather than a literal
     * 00:00 schedule. Preserve old behavior by treating it as WAKEUP here.
     */
    private fun DoseAnchorType.toLegacyFallbackTimeAnchor(): TimeAnchor? =
        when (this) {
            DoseAnchorType.MIDNIGHT -> TimeAnchor.WAKEUP
            else -> toTimeAnchorOrNull()
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
            TimeAnchor.DURING_WORKOUT -> null
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
            TimeAnchor.DURING_WORKOUT -> null
        }

    /**
     * Resolves the meal-aware state for a scheduled supplement dose.
     *
     * RATIONALE
     * ---------
     * Supplements and medications often include usage guidance such as:
     * - "Take with food"
     * - "Take on an empty stomach"
     * - "Avoid caffeine"
     *
     * These are medical recommendations, not strict constraints.
     * This function evaluates the current context and returns a
     * user-facing advisory state instead of blocking the dose.
     *
     * DESIGN PRINCIPLES
     * -----------------
     * • Never silently block a dose
     * • Prefer gentle reminders over enforcement
     * • Allow the UI to explain why a suggestion exists
     * • Support future extensibility
     */
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