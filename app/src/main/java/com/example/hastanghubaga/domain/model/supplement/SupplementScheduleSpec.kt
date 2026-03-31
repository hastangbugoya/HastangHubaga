package com.example.hastanghubaga.domain.model.supplement

import com.example.hastanghubaga.data.local.entity.meal.MealType
import com.example.hastanghubaga.domain.schedule.model.TimeAnchor
import kotlinx.datetime.LocalTime

sealed interface SupplementScheduleSpec {

    /**
     * User explicitly chooses times of day.
     * Example: 07:00, 12:00, 16:00
     */
    data class FixedTimes(
        val times: List<LocalTime>
    ) : SupplementScheduleSpec

    /**
     * User anchors supplement to one or more meals.
     * Example:
     * - With breakfast
     * - With breakfast and dinner
     * - 30 min after lunch
     */
    data class MealAnchored(
        val mealTypes: Set<MealType>,
        val offsetMinutes: Int = 0
    ) : SupplementScheduleSpec

    /**
     * User anchors supplement to one or more shared timeline anchors.
     *
     * Examples:
     * - Before workout
     * - During workout
     * - After workout
     * - Wakeup
     * - Sleep
     *
     * This is the preferred domain representation for persisted ANCHORED
     * supplement schedules. Concrete times should be resolved later using
     * shared anchor resolution context for the target date.
     */
    data class Anchored(
        val anchors: Set<TimeAnchor>,
        val offsetMinutes: Int = 0
    ) : SupplementScheduleSpec
}