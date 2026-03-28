package com.example.hastanghubaga.ui.timeline

import com.example.hastanghubaga.data.local.entity.meal.AkImportedMealEntity
import com.example.hastanghubaga.domain.model.activity.Activity
import com.example.hastanghubaga.domain.model.meal.Meal
import com.example.hastanghubaga.domain.model.supplement.SupplementWithUserSettings
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

    data class MealTimelineItem(
        override val time: LocalTime,
        val meal: Meal
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