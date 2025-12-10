package com.example.hastanghubaga.data.local.models

import androidx.room.Embedded
import androidx.room.Relation
import com.example.hastanghubaga.data.local.entity.meal.MealEntity
import com.example.hastanghubaga.data.local.entity.meal.MealNutritionEntity

data class MealJoinedRoom(
    @Embedded val meal: MealEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "mealId"
    )
    val nutrition: MealNutritionEntity?
)
