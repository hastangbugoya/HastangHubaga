package com.example.hastanghubaga.data.local.entity.supplement

/**
 * Defines anchor points used to schedule supplement doses relative
 * to meaningful times in a user’s daily routine.
 *
 * Each anchor represents a logical point in the day from which offset-based
 * dose times can be calculated (e.g., “30 minutes after BREAKFAST”).
 *
 * This abstraction allows the scheduling engine to adapt to different
 * user lifestyles (shift work, intermittent fasting, late wake-up times, etc.)
 * without requiring changes to supplement definitions.
 *
 * ## How anchors are used
 * - `MIDNIGHT` acts as the absolute base (00:00), unless overridden by
 *   `DailyStartTimeEntity` for users with shifted day cycles.
 * - Anchors like `BREAKFAST`, `LUNCH`, `DINNER`, and `WAKEUP` refer to
 *   user-defined or learned times.
 * - `CUSTOM_EVENT` supports future extensibility for arbitrary user-defined anchors.
 * - `ANYTIME` is used for supplements that have no timing sensitivity.
 *
 * These values are typically resolved into seconds-of-day via the
 * `event_default_times` table or user settings.
 */
enum class DoseAnchorType {

    /** Default anchor representing the start of the calendar day (00:00). */
    MIDNIGHT,

    /** User-defined wake-up time, used for morning-aligned doses. */
    WAKEUP,

    /** The user’s breakfast time or morning meal anchor. */
    BREAKFAST,

    /** Midday meal anchor. Used for supplements taken with lunch. */
    LUNCH,

    /** Evening meal anchor. Used for supplements requiring food. */
    DINNER,

    /** Time when caffeine or stimulant intake generally occurs. */
    CAFFEINE,

    /** Anchor for doses recommended before a workout session. */
    BEFORE_WORKOUT,

    /** Anchor for doses recommended immediately after a workout session. */
    AFTER_WORKOUT,

    /** Anchor for doses recommended immediately after a snack. */
    SNACK,

    /**
     * A flexible anchor reserved for future extension.
     * Allows users or the system to define new time-based events.
     */
    CUSTOM_EVENT,

    /**
     * Supplements with no preferred timing.
     * The app may schedule them in any convenient slot.
     */
    ANYTIME,
    ANY_MEAL
}

