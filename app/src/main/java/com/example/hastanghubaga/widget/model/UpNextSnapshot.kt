package com.example.hastanghubaga.widget.model

import com.example.hastanghubaga.ui.timeline.TodayUiRowType
import kotlinx.serialization.Serializable
/**
 * Widget-facing snapshot representing the single next scheduled timeline item.
 *
 * This model is **display-ready** and intentionally denormalized so the widget
 * does not need to perform any business logic, formatting, or time calculations.
 *
 * ### Design principles
 * - Nullable at the snapshot level: absence means "all done today"
 * - Preformatted strings only (no date/time math in widget)
 * - Stable, serializable shape for SharedPreferences storage
 * - Derived from UpcomingSchedule at snapshot-build time
 *
 * This class is **not** a domain model and must not be used for scheduling,
 * persistence, or decision-making.
 */
@Serializable
data class UpNextSnapshot(

    /**
     * Logical type of the upcoming item.
     *
     * Used by the widget to determine high-level behavior and presentation
     * (e.g., supplement vs meal vs activity).
     *
     * Must correspond to the same enum used by Today timeline rows so that
     * iconography and navigation semantics remain consistent.
     */
    val type: TodayUiRowType,

    /**
     * Primary display title for the upcoming item.
     *
     * Example:
     * - "Fish Oil"
     * - "Lunch"
     * - "Evening Walk"
     *
     * This value is already resolved and localized at snapshot build time.
     */
    val title: String,

    /**
     * Optional secondary line of text providing additional context.
     *
     * Example:
     * - Dosage ("2 capsules")
     * - Meal subtitle ("High protein")
     * - Activity note ("30 min")
     *
     * When null, the widget should omit the subtitle row entirely.
     */
    val subtitle: String?,

    /**
     * Human-readable, preformatted time label for display.
     *
     * Example:
     * - "7:00 AM"
     * - "18:30"
     *
     * This value is intended **only for display**. The widget must not attempt
     * to parse or compute with this string.
     */
    val timeLabel: String,

    /**
     * Stable icon identifier used by the widget to resolve the correct icon.
     *
     * This is intentionally a string (not a resource ID) to allow:
     * - snapshot persistence
     * - cross-module stability
     * - future theming or icon mapping changes
     *
     * Example values:
     * - "supplement"
     * - "meal"
     * - "activity"
     */
    val iconKey: String,

    /**
     * Reference ID of the underlying domain object.
     *
     * This ID may be used for:
     * - deep linking into the app
     * - navigation when the widget is tapped
     * - correlating the snapshot with timeline or database entities
     *
     * The widget itself must not interpret or modify this value.
     */
    val referenceId: Long
)