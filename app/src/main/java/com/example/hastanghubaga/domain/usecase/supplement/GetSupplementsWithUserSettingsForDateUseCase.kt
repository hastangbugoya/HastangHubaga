package com.example.hastanghubaga.domain.usecase.supplement

import com.example.hastanghubaga.data.local.dao.supplement.EventTimeDao
import com.example.hastanghubaga.data.local.entity.supplement.DoseAnchorType
import com.example.hastanghubaga.data.local.entity.supplement.FrequencyType
import com.example.hastanghubaga.domain.model.supplement.DoseCondition
import com.example.hastanghubaga.domain.model.supplement.MealAwareDoseState
import com.example.hastanghubaga.domain.model.supplement.MealLog
import com.example.hastanghubaga.domain.model.supplement.SupplementWithUserSettings
import com.example.hastanghubaga.domain.repository.supplement.SupplementRepository
import com.example.hastanghubaga.domain.time.DomainTimePolicy
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimePeriod
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
 * Returns supplements scheduled for a specific date, enriched with
 * user-specific timing and scheduling rules.
 *
 * This use case combines:
 * - Supplement definitions (frequency, anchor type, offsets)
 * - User-configured default anchor times (e.g., BREAKFAST → 08:00)
 * - Date-based scheduling logic
 *
 * The result represents the exact times each supplement should be taken
 * on the given date, accounting for:
 * - DAILY or interval-based frequencies
 * - Anchor-based scheduling (e.g., breakfast, lunch)
 * - User overrides and defaults
 *
 * This use case does not perform any persistence itself; it orchestrates
 * data retrieval and scheduling logic from repositories and DAOs.
 *
 * @param date The calendar date for which supplement schedules should be generated.
 * @return A [Flow] emitting a list of supplements with their computed scheduled times
 *         for the given date.
 */
class GetSupplementsWithUserSettingsForDateUseCase @Inject constructor(
    private val supplementRepository: SupplementRepository,
    private val eventTimeDao: EventTimeDao,
    private val clock: Clock = Clock.System
) {

    /**
     * Executes the scheduling logic for the provided date.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(
        date: LocalDate
    ): Flow<List<SupplementWithUserSettings>> =
        supplementRepository
            .getSupplementsForDate(date.toString())
            .mapLatest { supplements ->
                val mealsToday: List<MealLog> = emptyList<MealLog>()
                supplements
                    .filter { it.shouldTakeOn(date) }
                    .map { it.withScheduleFor(date, mealsToday) }
            }

    // ---------------------------------------------------------------------
    // Scheduling
    // ---------------------------------------------------------------------

    private suspend fun SupplementWithUserSettings.withScheduleFor(
        date: LocalDate,
        mealsToday: List<MealLog>
    ): SupplementWithUserSettings {

        val dosesPerDay: Double = resolveDosesPerDay()
        val offsetMinutes = supplement.offsetMinutes ?: 0
        val baseTime: LocalTime? = resolveAnchorTime(supplement.doseAnchorType, eventTimeDao)
        val scheduledTimes: List<LocalTime> =
            if (baseTime == null) {
                emptyList()
            } else {
                val anchorTime: LocalTime = baseTime
                val doseCount = kotlin.math.ceil(dosesPerDay).toInt()

                List(doseCount) { index ->
                    val totalSeconds =
                        anchorTime.toSecondOfDay() + (index * offsetMinutes * 60)

                    LocalTime.fromSecondOfDay(
                        ((totalSeconds % 86_400) + 86_400) % 86_400
                    )
                }
            }
        // Resolve dose state per scheduled time
        val doseState =
            scheduledTimes.firstOrNull()?.let { time ->
                resolveDoseState(
                    scheduledTime = time,
                    doseConditions = emptySet<DoseCondition>(),
                    mealsToday = mealsToday
                )
            } ?: MealAwareDoseState.Ready
        return copy(
            scheduledTimes = scheduledTimes,
            doseState = doseState
        )
    }

    private fun SupplementWithUserSettings.resolveDosesPerDay(): Double =
        userSettings?.preferredServingsPerDay
            ?: supplement.servingsPerDay

    /**
     * Resolves the effective anchor time for scheduling.
     *
     * MIDNIGHT is treated as a sentinel value meaning "no user-selected time".
     * In this case, we default to WAKEUP, which represents the earliest valid
     * time a supplement can reasonably be taken.
     *
     * This avoids scheduling supplements at 00:00 while still allowing the
     * MIDNIGHT enum to act as an unset / placeholder state.
     */
    private suspend fun resolveAnchorTime(
        anchor: DoseAnchorType,
        eventTimeDao: EventTimeDao
    ): LocalTime? {
        val effectiveAnchor =
            if (anchor == DoseAnchorType.MIDNIGHT)
                DoseAnchorType.WAKEUP
            else
                anchor

        return eventTimeDao
            .getDefault(effectiveAnchor)
            ?.timeSeconds
            ?.let {
                LocalTime.fromSecondOfDay(it)
            }
    }

    // ---------------------------------------------------------------------
    // Frequency rules
    // ---------------------------------------------------------------------
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
        // X Days does not apply
        val intervalDays = supplement.frequencyInterval ?: return false
        // Not taken before -> take it
        val lastTaken = supplement.lastTakenDate
            ?.let(LocalDate::parse)
            ?: return true
        // Get last dose date
        val nextEligibleDate =
            lastTaken.plus(intervalDays, DateTimeUnit.DAY)
        // today is the day or later -> take it
        return date >= nextEligibleDate
    }

    // ---------------------------------------------------------------------
    // Anchor fallbacks
    // ---------------------------------------------------------------------

    private fun fallbackAnchorTime(anchor: DoseAnchorType): LocalTime? =
        when (anchor) {
            DoseAnchorType.MIDNIGHT -> null
            DoseAnchorType.WAKEUP -> LocalTime(7, 0)
            DoseAnchorType.BREAKFAST -> LocalTime(8, 0)
            DoseAnchorType.LUNCH -> LocalTime(12, 0)
            DoseAnchorType.DINNER -> LocalTime(18, 0)
            DoseAnchorType.BEFORE_WORKOUT -> LocalTime(16, 30)
            DoseAnchorType.AFTER_WORKOUT -> LocalTime(17, 45)
            else -> null
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
     * These are *medical recommendations*, not strict constraints.
     * This function evaluates the current context and returns a
     * user-facing advisory state instead of blocking the dose.
     *
     * DESIGN PRINCIPLES
     * -----------------
     * • Never silently block a dose
     * • Prefer gentle reminders over enforcement
     * • Allow the UI to explain *why* a suggestion exists
     * • Support future extensibility (custom messages, overrides)
     *
     * @param scheduledTime The planned time for this dose
     * @param doseConditions Set of advisory conditions for the supplement
     * @param mealsToday List of meals already logged today (can be empty)
     *
     * @return A [MealAwareDoseState] indicating whether the dose is ready
     *         or if a friendly reminder should be shown to the user.
     */
    fun resolveDoseState(
        scheduledTime: LocalTime,
        doseConditions: Set<DoseCondition>,
        mealsToday: List<MealLog>
    ): MealAwareDoseState {
        val lastMealTime = mealsToday
            .maxByOrNull { it.time }
            ?.time
        /* ------------------------------------------------------------
           EMPTY STOMACH CHECK
           ------------------------------------------------------------ */
        if (DoseCondition.EMPTY_STOMACH in doseConditions) {
            val date = DomainTimePolicy.todayLocal(clock)
            val minutesSinceLastMeal =
                lastMealTime?.let { lastMeal ->
                    val mealDate =
                        if (lastMeal > scheduledTime)
                            date.minus(1, DateTimeUnit.DAY)
                        else
                            date

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
        /* ------------------------------------------------------------
           WITH FOOD CHECK
           ------------------------------------------------------------ */
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
        /* ------------------------------------------------------------
           AVOID CAFFEINE (INFORMATIONAL)
           ------------------------------------------------------------ */
        if (DoseCondition.AVOID_CAFFEINE in doseConditions) {
            return MealAwareDoseState.Advisory(
                reason = "Avoid caffeine close to this dose if possible."
            )
        }
        /* ------------------------------------------------------------
           DEFAULT: READY
           ------------------------------------------------------------ */
        return MealAwareDoseState.Ready
    }
}
