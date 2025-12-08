package com.example.hastanghubaga.data.local.entity.supplement

import androidx.room.Entity
/**
 * Stores per-day overrides for specific dose anchor times.
 *
 * This table allows the scheduling engine to adapt to exceptions in the
 * user's routine on a given date—such as waking up late, skipping breakfast,
 * traveling, or having a different workout time.
 *
 * ## Purpose
 * Normally, anchor times (e.g., WAKEUP, BREAKFAST) are supplied by:
 * - the `event_default_times` table (global defaults), or
 * - user settings (preferred daily schedule).
 *
 * When a user modifies the time for a single day—such as setting a custom
 * WAKEUP time for tomorrow—an entry is inserted here. The app uses this value
 * instead of the default for that specific date.
 *
 * ## Primary Key
 * The combination of:
 * - `date` (in `YYYY-MM-DD`) and
 * - `anchor` (a `DoseAnchorType`)
 *
 * uniquely identifies a custom override for that anchor on that day.
 *
 * ## Field Notes
 * - `timeSeconds`: Seconds since midnight representing the overridden time.
 * - `anchor`: The dose anchor being overridden (e.g., WAKEUP, DINNER).
 *
 * This allows the scheduler to compute dose times with full flexibility while
 * keeping historical accuracy for past days.
 */
@Entity(
    tableName = "event_daily_overrides",
    primaryKeys = ["date", "anchor"]
)
data class EventDailyOverrideEntity(
    val date: String,            // YYYY-MM-DD
    val anchor: DoseAnchorType,
    val timeSeconds: Int         // override time for this day
)

