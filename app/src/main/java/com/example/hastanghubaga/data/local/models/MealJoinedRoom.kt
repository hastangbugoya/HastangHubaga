package com.example.hastanghubaga.data.local.models

import androidx.room.Embedded
import androidx.room.Relation
import com.example.hastanghubaga.data.local.entity.meal.MealEntity
import com.example.hastanghubaga.data.local.entity.meal.MealNutritionEntity
import com.example.hastanghubaga.data.local.entity.meal.MealScheduleEntity
import com.example.hastanghubaga.data.local.entity.meal.MealScheduleWithTimes

/**
 * Joined Room graph for a meal template.
 *
 * This now mirrors the activity-side editor loading pattern:
 *
 * - meal template core fields
 * - optional meal nutrition
 * - optional persisted schedule graph
 *
 * The schedule relation is nullable because a meal may exist without any
 * schedule configured yet.
 */
data class MealJoinedRoom(
    @Embedded
    val meal: MealEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "mealId"
    )
    val nutrition: MealNutritionEntity?,

    @Relation(
        entity = MealScheduleEntity::class,
        parentColumn = "id",
        entityColumn = "mealId"
    )
    val schedule: MealScheduleWithTimes?
)