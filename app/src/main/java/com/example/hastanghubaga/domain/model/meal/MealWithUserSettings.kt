package com.example.hastanghubaga.domain.model.meal

import java.time.LocalTime

data class MealWithUserSettings(
    /** Core domain definition (what the meal is) */
    val meal: Meal,

    /**
     * Per-user configuration and preferences.
     *
     * Nullable because:
     * - user may not have customized this meal yet
     * - defaults may be used instead
     */
    val userSettings: UserMealSettings?,

    /**
     * Computed times this meal is scheduled for today.
     *
     * - Derived by a use case
     * - Depends on date, preferences, plan, reminders, etc.
     * - Empty means "not scheduled today"
     */
    val scheduledTimes: List<LocalTime> = emptyList(),

    /**
     * Day-specific state (logged, skipped, planned, etc.).
     *
     * This is intentionally NOT inside UserMealSettings because:
     * - it varies by date
     * - it is contextual, not a preference
     */
    val mealState: MealState = MealState.Planned
)

