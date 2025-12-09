package com.example.hastanghubaga.core.backup.model

import com.example.hastanghubaga.data.local.entity.meal.MealNutritionEntity
import kotlinx.serialization.Serializable
import com.example.hastanghubaga.data.local.entity.supplement.*
import com.example.hastanghubaga.data.local.entity.user.SupplementUserSettingsEntity
import com.example.hastanghubaga.data.local.entity.activity.ActivityEntity
import com.example.hastanghubaga.data.local.entity.meal.*
import com.example.hastanghubaga.data.local.entity.user.UserNutritionGoalsEntity

/**
 * Complete encrypted backup payload for all app tables.
 *
 * This is the only file that BackupManager serializes.
 * Restore simply deserializes and re-inserts the data.
 */
@Serializable
data class BackupPayload(
    // Activity
    val activity: List<ActivityEntity> = emptyList(),

    // Meal
    val meals: List<MealEntity> = emptyList(),
    val mealNutritionEntity: List<MealNutritionEntity> = emptyList(),

    // Nutrition
    val nutritionGoals: List<UserNutritionGoalsEntity> = emptyList(),

    // Supplements & base data
    val supplements: List<SupplementEntity> = emptyList(),
    val supplementIngredients: List<SupplementIngredientEntity> = emptyList(),
    val ingredients: List<IngredientEntity> = emptyList(),

    // User settings per supplement
    val userSettings: List<SupplementUserSettingsEntity> = emptyList(),

    // Dose logs
    val dailyLogs: List<SupplementDailyLogEntity> = emptyList(),

    // Time-based scheduling
    val dailyStartTimes: List<DailyStartTimeEntity> = emptyList(),
    val eventDefaultTimes: List<EventDefaultTimeEntity> = emptyList(),
    val eventDailyOverrides: List<EventDailyOverrideEntity> = emptyList()
)

