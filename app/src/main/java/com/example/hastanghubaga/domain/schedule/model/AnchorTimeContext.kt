package com.example.hastanghubaga.domain.schedule.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

/**
 * Immutable scheduling context used to resolve a [TimeAnchor] for a specific day.
 *
 * This model provides the full set of anchor-time inputs needed by shared
 * scheduling logic, such as meals, supplements, and future anchored timeline
 * features.
 *
 * ---
 * 🧠 Resolution Layers
 *
 * Resolution is performed using a layered lookup strategy:
 *
 * 1. Workout-based anchors (if applicable)
 * 2. Exact date override
 * 3. Day-of-week override
 * 4. Default anchor time
 *
 * ---
 * 🏋️ Workout Anchors (NEW)
 *
 * Workout-aware anchors (e.g., BEFORE_WORKOUT, DURING_WORKOUT, AFTER_WORKOUT)
 * may resolve dynamically using [workoutAnchors].
 *
 * These are derived externally (typically in BuildTodayTimelineUseCase)
 * from activities marked as workouts:
 *
 * - BEFORE_WORKOUT → workout start
 * - DURING_WORKOUT → workout start (first pass)
 * - AFTER_WORKOUT → workout end (fallback to start)
 *
 * If no workouts exist, resolution falls back to standard overrides/defaults.
 *
 * ---
 * 🍽️ Non-Workout Anchors
 *
 * Anchors such as BREAKFAST, LUNCH, DINNER are NOT affected by workout logic
 * and continue to use override/default resolution only.
 *
 * ---
 * 🧪 Design Notes
 *
 * - This model is intentionally passive (no logic)
 * - It enables pure, testable anchor resolution
 * - It decouples scheduling from data sources (activities, meals, etc.)
 *
 * ---
 * @property date The calendar date for which anchor resolution is performed.
 * @property defaultTimes Baseline anchor times keyed by [TimeAnchor].
 * @property dayOfWeekOverrides Day-specific overrides.
 * @property dateOverrides Exact-date overrides (highest priority).
 * @property workoutAnchors Optional workout windows used for dynamic anchor resolution.
 */
data class AnchorTimeContext(
    val date: LocalDate,
    val defaultTimes: Map<TimeAnchor, LocalTime>,
    val dayOfWeekOverrides: Map<AnchorDayKey, LocalTime> = emptyMap(),
    val dateOverrides: Map<AnchorDateKey, LocalTime> = emptyMap(),

    // NEW (safe default, does not break existing callers)
    val workoutAnchors: List<WorkoutAnchorWindow> = emptyList()
)

/**
 * Represents a workout time window used for anchor resolution.
 *
 * This is a lightweight domain model derived from Activity.
 * It intentionally avoids depending on the full Activity model
 * to keep scheduling logic decoupled and testable.
 *
 * @property activityId Identifier of the source activity
 * @property startTime Workout start time (required)
 * @property endTime Optional workout end time
 * @property label Optional display label (e.g., "Leg Day")
 */
data class WorkoutAnchorWindow(
    val activityId: Long,
    val startTime: LocalTime,
    val endTime: LocalTime? = null,
    val label: String? = null
)