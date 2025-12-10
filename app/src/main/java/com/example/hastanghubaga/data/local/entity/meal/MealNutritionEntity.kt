package com.example.hastanghubaga.data.local.entity.meal

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "meal_nutrition")
data class MealNutritionEntity(

    @PrimaryKey
    val id: Long = 0L,

    val mealId: Long,

    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val calories: Double?,
    val sodium: Double?,
    val cholesterol: Double?,
    val fiber: Double?
)
