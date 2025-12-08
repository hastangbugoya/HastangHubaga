package com.example.hastanghubaga.data.local.entity.supplement

/**
 * Represents the repetition schedule for a supplement's dosing pattern.
 *
 * A supplement may be taken every day, only on specific days of the week, or
 * at a fixed interval such as every 2 or 3 days. This enum defines the three
 * supported scheduling modes that determine how the app generates a user's
 * supplement timeline.
 *
 * ## Types
 * ### `DAILY`
 * The supplement is taken once per day. The scheduler assumes it should appear
 * **every calendar day**, unless manually disabled.
 *
 * ### `WEEKLY`
 * The supplement is taken on specified days of the week.
 * - The corresponding entity field (e.g., `weeklyDays`) stores which weekdays.
 * - Often used for supplements such as Vitamin D (weekly high-dose protocol).
 *
 * ### `EVERY_X_DAYS`
 * The supplement repeats on a fixed interval independent of calendar weeks.
 * - For example, “every 2 days” or “every 3 days.”
 * - The interval is tracked relative to a reference date stored in the
 *   supplement settings.
 *
 * ## Usage
 * This enum is used by:
 * - the scheduling engine to compute the next dose date
 * - the UI to render supplement frequency descriptions
 * - import/export and database persistence of supplement metadata
 *
 * By standardizing frequency patterns, the app can reliably convert supplement
 * rules into daily schedules.
 */
enum class FrequencyType {
    DAILY,
    WEEKLY,
    EVERY_X_DAYS
}
