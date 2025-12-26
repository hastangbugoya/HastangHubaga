package com.example.hastanghubaga.domain.model.nutrition

import androidx.room.PrimaryKey

data class NutrientGoal(
    val ingredientId: Long,
    val target: Double,
    val upperLimit: Double?,
    val unit: String,
    val isEnabled: Boolean
)