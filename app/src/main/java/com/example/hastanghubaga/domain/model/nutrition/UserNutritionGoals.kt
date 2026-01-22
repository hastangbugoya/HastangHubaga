package com.example.hastanghubaga.domain.model.nutrition

data class UserNutritionGoals(
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