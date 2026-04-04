package com.example.hastanghubaga.data.local.entity.meal

import androidx.room.Embedded
import androidx.room.Relation
import com.example.hastanghubaga.data.local.entity.meal.MealScheduleAnchoredTimeEntity
import com.example.hastanghubaga.data.local.entity.meal.MealScheduleEntity
import com.example.hastanghubaga.data.local.entity.meal.MealScheduleFixedTimeEntity

/**
 * Full schedule graph for a meal.
 *
 * This mirrors the activity scheduling relation model so the same
 * scheduling pipeline and reusable UI can operate on meals.
 *
 * Structure:
 *
 * - parent: MealScheduleEntity
 * - children:
 *     - fixedTimes (when timingType = FIXED_TIMES)
 *     - anchoredTimes (when timingType = ANCHORED)
 *
 * ## Why this exists
 *
 * Room cannot automatically join multiple child tables into one object.
 * This relation model allows:
 *
 * - repository to return a complete schedule graph
 * - domain layer to evaluate schedule without additional queries
 * - UI to bind directly to scheduling data
 *
 * ## Important behavior
 *
 * - Only ONE of the child lists should be used at runtime:
 *     - FIXED_TIMES -> use [fixedTimes]
 *     - ANCHORED -> use [anchoredTimes]
 *
 * - The other list should be treated as ignored/empty
 *
 * ## Future use
 *
 * This structure will feed:
 *
 * - BuildPlannedMealOccurrencesForDateUseCase (future)
 * - Timeline builder
 * - Scheduling editor UI
 */
data class MealScheduleWithTimes(

    @Embedded
    val schedule: MealScheduleEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "scheduleId",
        entity = MealScheduleFixedTimeEntity::class
    )
    val fixedTimes: List<MealScheduleFixedTimeEntity> = emptyList(),

    @Relation(
        parentColumn = "id",
        entityColumn = "scheduleId",
        entity = MealScheduleAnchoredTimeEntity::class
    )
    val anchoredTimes: List<MealScheduleAnchoredTimeEntity> = emptyList()
)
