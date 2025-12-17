package com.example.hastanghubaga.data.local.entity.user

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.hastanghubaga.domain.model.nutrition.NutritionGoalType
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "nutrition_goals")
data class UserNutritionGoalsEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val type: NutritionGoalType,
    val name: String, // "Summer Cut", "Lean Bulk", etc.
    val startDate: Long, // epoch millis - start of goal
    val endDate: Long?, // epoch millis - end of goal (may be null)
    val dailyProteinTarget: Double,
    val dailyFatTarget: Double,
    val dailyCarbTarget: Double,
    val dailyCalorieTarget: Double? = null,

    val sodiumLimitMg: Double? = null,
    val cholesterolLimitMg: Double? = null,
    val fiberTargetGrams: Double? = null,

    val isActive: Boolean = false
)