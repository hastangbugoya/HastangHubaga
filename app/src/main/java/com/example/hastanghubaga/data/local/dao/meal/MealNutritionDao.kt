package com.example.hastanghubaga.data.local.dao.meal

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.hastanghubaga.data.local.entity.meal.MealNutritionEntity
import kotlinx.coroutines.flow.Flow

/**
 * Transitional row shape kept only so older callers can still compile while
 * meals finish moving from timestamped event rows to template + schedule rows.
 *
 * IMPORTANT:
 * - MealEntity no longer stores a timestamp.
 * - Date-based meal nutrition reads must eventually come from a true meal log /
 *   occurrence-aware actual-meal table, not from the meal template table.
 */
data class MealNutritionAtTimeRow(
    val timestamp: Long,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val calories: Int,
    val sodium: Double?,
    val cholesterol: Double?,
    val fiber: Double?
)

@Dao
interface MealNutritionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNutrition(nutrition: MealNutritionEntity)

    @Query("SELECT * FROM meal_nutrition WHERE mealId = :mealId LIMIT 1")
    suspend fun getNutritionForMeal(mealId: Long): MealNutritionEntity?

    @Query("DELETE FROM meal_nutrition WHERE mealId = :mealId")
    suspend fun deleteNutrition(mealId: Long)

    /**
     * Transitional placeholder.
     *
     * This previously joined meal_nutrition -> meals and filtered by
     * meals.timestamp. That is no longer valid because meals are now reusable
     * templates and do not carry occurrence/log timestamps.
     *
     * We intentionally return an empty result until the real meal log /
     * occurrence-aware nutrition query is implemented.
     */
    @Query(
        """
        SELECT
            0 AS timestamp,
            n.protein AS protein,
            n.carbs AS carbs,
            n.fat AS fat,
            n.calories AS calories,
            n.sodium AS sodium,
            n.cholesterol AS cholesterol,
            n.fiber AS fiber
        FROM meal_nutrition n
        WHERE :startMillis > :endMillis
        ORDER BY timestamp ASC
        """
    )
    fun observeNutritionForMealsInRange(
        startMillis: Long,
        endMillis: Long
    ): Flow<List<MealNutritionAtTimeRow>>
}