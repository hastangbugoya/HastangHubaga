package com.example.hastanghubaga.data.local.entity.supplement

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Defines the default time-of-day for each dose anchor used throughout the app.
 *
 * Dose anchors (e.g., WAKEUP, BREAKFAST, BEFORE_WORKOUT) are reference points
 * used by the scheduling engine to determine when supplements should be taken.
 * These defaults represent the user's typical daily routine.
 *
 * ## Purpose
 * This table stores global, persistent default event times that apply to
 * *all days*, unless a specific override for a date exists in
 * [EventDailyOverrideEntity].
 *
 * These defaults are used in:
 * - computing supplement schedules
 * - determining notification times
 * - generating the day's timeline in the Today screen
 *
 * ## Example
 * - WAKEUP might default to 07:00 → `timeSeconds = 25200`
 * - BREAKFAST might default to 08:00 → `timeSeconds = 28800`
 *
 * ## Field Notes
 * - `anchor` is the primary key, ensuring one default per anchor.
 * - `timeSeconds` is the number of seconds after midnight
 *   (from `LocalTime.toSecondOfDay()`).
 *
 * ## Override Behavior
 * If the user changes the time on a *specific* day, that override is stored in
 * [EventDailyOverrideEntity], and the scheduler automatically prefers the
 * override for that date.
 */
@Serializable
@Entity(tableName = "event_default_times")
data class EventDefaultTimeEntity(
    @PrimaryKey
    val anchor: DoseAnchorType,
    val timeSeconds: Int // LocalTime.toSecondOfDay()
)


