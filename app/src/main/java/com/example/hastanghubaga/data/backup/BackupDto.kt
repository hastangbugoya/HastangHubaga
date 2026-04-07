package com.example.hastanghubaga.data.backup

import com.example.hastanghubaga.data.local.entity.meal.MealEntity
import com.example.hastanghubaga.data.local.entity.meal.MealNutritionEntity
import com.example.hastanghubaga.data.local.entity.user.UserNutritionGoalsEntity
import com.example.hastanghubaga.data.local.entity.supplement.*
import com.example.hastanghubaga.data.local.entity.user.SupplementUserSettingsEntity


/**
 * Data Transfer Object representing a complete encrypted backup file.
 *
 * Modify this only when the database schema evolves.
 * Include a version so future versions can migrate old backups.
 */
data class BackupDto(
    val version: Int = 1,
    val timestamp: Long = System.currentTimeMillis(),

    val supplements: List<SupplementEntity>,
    val ingredients: List<IngredientEntity>,
    val supplementIngredients: List<SupplementIngredientEntity>,
    val supplementSettings: List<SupplementUserSettingsEntity>,
    val dailyLogs: List<SupplementDailyLogEntity>,
    val eventDefaults: List<EventDefaultTimeEntity>,
    val eventOverrides: List<EventDailyOverrideEntity>,
    val dailyStartTimes: List<DailyStartTimeEntity>,
    val meal : List<MealEntity>,
    val mealNutrition : List<MealNutritionEntity>,
    val nutritionGoal: List<UserNutritionGoalsEntity>
)

