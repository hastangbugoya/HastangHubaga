package com.example.hastanghubaga.domain.schedule.timing

import com.example.hastanghubaga.domain.schedule.model.AnchorDateKey
import com.example.hastanghubaga.domain.schedule.model.AnchorDayKey
import com.example.hastanghubaga.domain.schedule.model.AnchorTimeContext
import com.example.hastanghubaga.domain.schedule.model.TimeAnchor
import kotlinx.datetime.LocalTime
import javax.inject.Inject

/**
 * Resolves the effective time for a [TimeAnchor] within a given [AnchorTimeContext].
 *
 * ---
 * 🧠 Resolution Order (Highest → Lowest Priority)
 *
 * 1. Workout-based resolution (for workout anchors only)
 * 2. Exact date override
 * 3. Day-of-week override
 * 4. Default anchor time
 *
 * ---
 * 🏋️ Workout-Aware Anchors
 *
 * The following anchors may resolve dynamically based on workout activities
 * provided via [AnchorTimeContext]:
 *
 * - BEFORE_WORKOUT → workout start
 * - DURING_WORKOUT → workout start (first pass)
 * - AFTER_WORKOUT → workout end (fallback to start if null)
 *
 * If no workouts exist in context, resolution falls back to the standard
 * override/default pipeline.
 *
 * ---
 * 🍽️ Non-Workout Anchors
 *
 * Anchors such as BREAKFAST, LUNCH, DINNER, etc. are unaffected by workout logic
 * and continue to resolve via override/default behavior.
 *
 * ---
 * ⚠️ Design Notes
 *
 * - This use case remains pure and deterministic
 * - It does NOT fetch activities itself
 * - It relies entirely on [AnchorTimeContext] for inputs
 * - This keeps supplements, meals, and activities decoupled
 *
 * ---
 * @return The resolved [LocalTime], or null if no resolution is possible
 */
class ResolveAnchorTimeUseCase @Inject constructor() {

    operator fun invoke(
        anchor: TimeAnchor,
        context: AnchorTimeContext
    ): LocalTime? {

        // 1. Workout-based resolution (NEW, highest priority)
        resolveWorkoutAnchor(anchor, context)?.let { return it }

        // 2. Exact date override
        val dateOverrideKey = AnchorDateKey(
            anchor = anchor,
            date = context.date
        )
        context.dateOverrides[dateOverrideKey]?.let { return it }

        // 3. Day-of-week override
        val dayOverrideKey = AnchorDayKey(
            anchor = anchor,
            dayOfWeek = context.date.dayOfWeek
        )
        context.dayOfWeekOverrides[dayOverrideKey]?.let { return it }

        // 4. Default anchor time
        return context.defaultTimes[anchor]
    }

    /**
     * Attempts to resolve workout-related anchors using workout context.
     *
     * Returns null if:
     * - Anchor is not workout-related
     * - No workouts exist for the day
     */
    private fun resolveWorkoutAnchor(
        anchor: TimeAnchor,
        context: AnchorTimeContext
    ): LocalTime? {
        val workouts = context.workoutAnchors
        if (workouts.isEmpty()) return null

        // First-pass strategy: use earliest workout of the day
        val primaryWorkout = workouts.minByOrNull { it.startTime } ?: return null

        return when (anchor) {
            TimeAnchor.BEFORE_WORKOUT -> primaryWorkout.startTime
            TimeAnchor.DURING_WORKOUT -> primaryWorkout.startTime
            TimeAnchor.AFTER_WORKOUT -> primaryWorkout.endTime ?: primaryWorkout.startTime
            else -> null
        }
    }
}