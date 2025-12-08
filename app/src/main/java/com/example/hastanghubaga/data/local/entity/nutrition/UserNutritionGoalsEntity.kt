package com.example.hastanghubaga.data.local.entity.nutrition

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.hastanghubaga.domain.model.NutritionGoal

@Entity(tableName = "nutrition_goals")
data class UserNutritionGoalsEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val type: NutritionGoalType,
    val name: String, // "Summer Cut", "Lean Bulk", etc.

    val dailyProteinTarget: Double,
    val dailyFatTarget: Double,
    val dailyCarbTarget: Double,
    val dailyCalorieTarget: Double? = null,

    val sodiumLimitMg: Double? = null,
    val cholesterolLimitMg: Double? = null,
    val fiberTargetGrams: Double? = null,

    val isActive: Boolean = false
)




