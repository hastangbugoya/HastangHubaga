package com.example.hastanghubaga.domain.usecase.supplement

import com.example.hastanghubaga.data.local.dao.supplement.EventTimeDao
import com.example.hastanghubaga.data.local.entity.supplement.DoseAnchorType
import com.example.hastanghubaga.data.local.entity.supplement.FrequencyType
import com.example.hastanghubaga.domain.model.supplement.DoseCondition
import com.example.hastanghubaga.domain.model.supplement.MealAwareDoseState
import com.example.hastanghubaga.domain.model.supplement.MealLog
import com.example.hastanghubaga.domain.model.supplement.SupplementWithUserSettings
import com.example.hastanghubaga.domain.repository.supplement.SupplementRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest
import java.time.LocalDate
import java.time.LocalTime
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
    private val eventTimeDao: EventTimeDao
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

        val dosesPerDay = resolveDosesPerDay()
        val offsetMinutes = supplement.offsetMinutes ?: 0
        val baseTime = this@GetSupplementsWithUserSettingsForDateUseCase
            .resolveAnchorTime(supplement.doseAnchorType, eventTimeDao)
        val scheduledTimes =
            if (baseTime == null) emptyList()
            else List(dosesPerDay) { index ->
                baseTime.plusMinutes((index * offsetMinutes).toLong())
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

    private fun SupplementWithUserSettings.resolveDosesPerDay(): Int =
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
                LocalTime.ofSecondOfDay(it.toLong())
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
        val lastTaken = supplement.lastTakenDate?.let(LocalDate::parse)
        val interval = supplement.frequencyInterval ?: return false
        return lastTaken == null || lastTaken.plusDays(interval.toLong()) == date
    }

    // ---------------------------------------------------------------------
    // Anchor fallbacks
    // ---------------------------------------------------------------------

    private fun fallbackAnchorTime(anchor: DoseAnchorType): LocalTime? =
        when (anchor) {
            DoseAnchorType.MIDNIGHT -> null
            DoseAnchorType.WAKEUP -> LocalTime.of(7, 0)
            DoseAnchorType.BREAKFAST -> LocalTime.of(8, 0)
            DoseAnchorType.LUNCH -> LocalTime.of(12, 0)
            DoseAnchorType.DINNER -> LocalTime.of(18, 0)
            DoseAnchorType.BEFORE_WORKOUT -> LocalTime.of(16, 30)
            DoseAnchorType.AFTER_WORKOUT -> LocalTime.of(17, 45)
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

            val minutesSinceLastMeal =
                lastMealTime?.let {
                    java.time.Duration.between(it, scheduledTime).toMinutes()
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

            val hasRecentMeal =
                lastMealTime != null &&
                        java.time.Duration
                            .between(lastMealTime, scheduledTime)
                            .toMinutes() <= 60

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
