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
    val calories: Int,
    val sodium: Double? = null,
    val cholesterol: Double? = null,
    val fiber: Double? = null
)
