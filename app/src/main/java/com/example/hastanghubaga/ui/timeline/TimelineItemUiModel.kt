package com.example.hastanghubaga.ui.timeline

import com.example.hastanghubaga.data.local.entity.meal.MealType
import com.example.hastanghubaga.domain.model.activity.ActivityType
import com.example.hastanghubaga.domain.model.supplement.MealAwareDoseState
import java.time.LocalTime

/**
 * UI-normalized timeline row model.
 *
 * This sealed interface represents ALL items that can appear
 * in the Today timeline (meals, supplements, activities).
 *
 * Design goals:
 * - Single LazyColumn source
 * - Stable UI keys
 * - Exhaustive rendering via sealed types
 * - No domain leakage (UI-only model)
 */
sealed interface TimelineItemUiModel {

    /** Stable UI key for LazyColumn */
    val key: String

    /** Source entity ID (supplementId, mealId, activityId) */
    val id: Long

    /** Time this item occurs */
    val time: LocalTime

    /** Primary display text */
    val title: String

    /** Optional secondary text */
    val subtitle: String?

    /** Row category for styling & behavior */
    val rowType: TodayUiRowType

    data class Supplement(
        override val id: Long,
        override val time: LocalTime,
        override val title: String,
        override val subtitle: String?,
        val doseState: MealAwareDoseState?
    ) : TimelineItemUiModel {
        override val rowType = TodayUiRowType.SUPPLEMENT
        override val key = "${rowType.name}-$id-$time"
    }

    data class Meal(
        override val id: Long,
        override val time: LocalTime,
        override val title: String,
        override val subtitle: String?,
        val type: MealType
    ) : TimelineItemUiModel {
        override val rowType = TodayUiRowType.MEAL
        override val key = "${rowType.name}-$id-$time"
    }

    data class Activity(
        override val id: Long,
        override val time: LocalTime,
        override val title: String,
        override val subtitle: String?,
        val activityType: ActivityType,
        val endTime: LocalTime?
    ) : TimelineItemUiModel {
        override val rowType = TodayUiRowType.ACTIVITY
        override val key = "${rowType.name}-$id-$time"
    }
}
