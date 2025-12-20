package com.example.hastanghubaga.data.local.entity.supplement
/**
 * Represents the **anchor point in time** around which a supplement dose
 * should be scheduled.
 *
 * -------------------------------------------------------------------------
 * OVERVIEW
 * -------------------------------------------------------------------------
 * A `DoseAnchorType` answers the question:
 *
 * 👉 “What real-world event should this supplement be tied to?”
 *
 * Examples:
 * - A meal (Breakfast, Lunch, Dinner)
 * - A daily routine (Wakeup, Midnight)
 * - An activity (Workout)
 * - A flexible or abstract concept (Anytime, Custom)
 *
 * Anchors are later resolved into concrete `LocalTime` values using:
 * - User-defined defaults (EventDefaultTimeEntity)
 * - Day-of-week overrides (EventDayOfWeekTimeEntity)
 * - Fallback system defaults
 *
 * Offsets (positive or negative minutes) may then be applied to support:
 * - “10 minutes before breakfast”
 * - “30 minutes after dinner”
 *
 * -------------------------------------------------------------------------
 * RATIONALE
 * -------------------------------------------------------------------------
 * Anchors are modeled as an enum instead of raw times because:
 *
 * 1) Real-world schedules are **semantic**, not just clock-based
 *    - “After workout” is more meaningful than “5:45 PM”
 *
 * 2) User schedules change
 *    - Breakfast might be 8am on weekdays, 10am on weekends
 *
 * 3) Anchors enable **smart rescheduling**
 *    - If a meal is skipped, the dose can become “Pending”
 *
 * 4) Anchors enable **conflict handling**
 *    - Multiple doses tied to the same meal
 *    - Stacking vs staggering logic
 *
 * -------------------------------------------------------------------------
 * MEAL AWARENESS
 * -------------------------------------------------------------------------
 * Some anchors are associated with meals via [mealType].
 *
 * This allows the system to:
 * - Detect whether a dose depends on a meal
 * - Mark doses as `PendingMeal` if the meal hasn’t occurred
 * - Support `ANY_MEAL` logic (first available meal)
 *
 * Use:
 * ```
 * anchor.mealType != null
 * ```
 * to determine if an anchor is meal-based.
 *
 * -------------------------------------------------------------------------
 * FUTURE USAGE EXAMPLES
 * -------------------------------------------------------------------------
 *
 * Scheduling:
 * ```
 * val baseTime = resolveAnchorTime(anchor, date)
 * val finalTime = baseTime.plusMinutes(offsetMinutes)
 * ```
 *
 * Meal-aware UI:
 * ```
 * if (anchor.mealType != null && mealNotEatenYet) {
 *     doseState = PendingMeal
 * }
 * ```
 *
 * Conflict handling:
 * ```
 * if (two doses resolve to same time) {
 *     stack OR stagger
 * }
 * ```
 */
enum class DoseAnchorType(
    /** Optional meal this anchor is associated with */
    val mealType: MealType?,
    val isSentinel: Boolean = false
) {

    /** Absolute start of the day (00:00).
     *  Useful for fasting supplements or strict schedules.
     */
    MIDNIGHT(null, isSentinel = true),

    /** Time the user wakes up.
     *  Common for hydration, thyroid meds, or baseline supplements.
     */
    WAKEUP(null),

    /** Tied specifically to breakfast.
     *  Meal-dependent supplements (e.g., fat-soluble vitamins).
     */
    BREAKFAST(MealType.BREAKFAST),

    /** Tied specifically to lunch. */
    LUNCH(MealType.LUNCH),

    /** Tied specifically to dinner. */
    DINNER(MealType.DINNER),

    /** A light meal or snack.
     *  Useful for supplements that shouldn’t be taken on an empty stomach.
     */
    SNACK(MealType.SNACK),

    /** Conceptual anchor for caffeine-related supplements.
     *  Used mainly for filtering, warnings, or UI grouping.
     */
    CAFFEINE(null),

    /** Scheduled before workout begins.
     *  Common for pre-workout supplements.
     */
    BEFORE_WORKOUT(null),

    /** Scheduled after workout completes.
     *  Common for recovery supplements.
     */
    AFTER_WORKOUT(null),

    /** User-defined or externally triggered event.
     *  Allows future extensibility without schema changes.
     */
    CUSTOM_EVENT(null),

    /** Tied to *any* meal.
     *  Resolved to the first applicable meal of the day.
     */
    ANY_MEAL(MealType.ANY),
    SLEEP(null),

    /** No strict anchor.
     *  Used for flexible or “take anytime today” supplements.
     */
    ANYTIME(null)
}

enum class MealType {
    BREAKFAST, LUNCH, DINNER, SNACK, ANY
}

fun DoseAnchorType.isUserVisible(): Boolean = !isSentinel

fun Iterable<DoseAnchorType>.userVisibleAnchors(): List<DoseAnchorType> =
    filter { it.isUserVisible() }

/**
 * Sentinel anchors define system-level temporal boundaries.
 *
 * Rules:
 * - Sentinels MUST be persisted
 * - Sentinels MUST NOT be shown in UI
 * - Sentinels MAY be used for scheduling math and defaults
 * - Sentinels MUST have deterministic times
 */
object SentinelAnchors {

    val REQUIRED = setOf(
        DoseAnchorType.MIDNIGHT
    )
}


