package com.example.hastanghubaga.ui.timeline

import com.example.hastanghubaga.data.local.entity.meal.AkImportedMealEntity
import com.example.hastanghubaga.domain.model.activity.Activity
import com.example.hastanghubaga.domain.model.meal.Meal
import com.example.hastanghubaga.domain.model.supplement.SupplementWithUserSettings
import com.example.hastanghubaga.domain.schedule.model.TimeAnchor
import kotlinx.datetime.LocalTime

sealed interface TimelineItem {
    val time: LocalTime

    data class SupplementTimelineItem(
        override val time: LocalTime,
        val isTaken: Boolean = false,
        val supplement: SupplementWithUserSettings
    ) : TimelineItem

    data class ActivityTimelineItem(
        override val time: LocalTime,
        val activity: Activity
    ) : TimelineItem

    /**
     * Native HH meal timeline row.
     *
     * Important:
     * - [resolvedAnchor] is derived anchor behavior only
     * - It does NOT change the meal's actual type
     * - It does NOT affect current timeline rendering or ordering
     * - It prepares meals to act as anchor providers later
     */
    data class MealTimelineItem(
        override val time: LocalTime,
        val meal: Meal,
        val resolvedAnchor: TimeAnchor? = null
    ) : TimelineItem

    /**
     * Read-only AK imported meal timeline row.
     *
     * Important:
     * - This is NOT a native HH meal
     * - This is backed by ak_imported_meals materialization only
     * - Do not use this to imply linking, merging, or assignment to HH meals
     */
    data class ImportedMealTimelineItem(
        override val time: LocalTime,
        val meal: AkImportedMealEntity
    ) : TimelineItem

    data class SupplementDoseLogTimelineItem(
        val doseLogId: Long,
        val supplementId: Long,
        val title: String,                 // display name
        override val time: LocalTime,      // actual taken time
        val amount: Double?,
        val unit: String?,                 // or your SupplementDoseUnit
        val scheduledTime: LocalTime? = null // optional
    ) : TimelineItem
}