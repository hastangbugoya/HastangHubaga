package com.example.hastanghubaga.domain.model.supplement

import com.example.hastanghubaga.data.local.entity.meal.MealType
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
}
