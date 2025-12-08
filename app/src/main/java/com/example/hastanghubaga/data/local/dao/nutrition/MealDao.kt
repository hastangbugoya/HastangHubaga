package com.example.hastanghubaga.data.local.dao.nutrition

import androidx.room.*
import com.example.hastanghubaga.data.local.entity.nutrition.MealEntity
import com.example.hastanghubaga.data.local.entity.nutrition.MealType
import kotlinx.coroutines.flow.Flow

@Dao
interface MealDao {

    // ---------------------------------------------------------
    // INSERT
    // ---------------------------------------------------------
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeal(meal: MealEntity): Long

    @Delete
    suspend fun deleteMeal(meal: MealEntity)

    @Query("DELETE FROM meals WHERE id = :id")
    suspend fun deleteMeal(id: Long)

    // ---------------------------------------------------------
    // QUERY BY DATE
    // ---------------------------------------------------------
    @Query("""
        SELECT * FROM meals 
        WHERE date = :date 
        ORDER BY timestamp ASC
    """)
    fun observeMealsForDate(date: String): Flow<List<MealEntity>>

    @Query("""
        SELECT * FROM meals 
        WHERE date = :date 
        ORDER BY timestamp ASC
    """)
    suspend fun getMealsForDate(date: String): List<MealEntity>

    // ---------------------------------------------------------
    // QUERY BY MEAL TYPE (breakfast, snack, etc.)
    // ---------------------------------------------------------
    @Query("""
        SELECT * FROM meals
        WHERE type = :type
        ORDER BY timestamp DESC
    """)
    fun observeMealsOfType(type: MealType): Flow<List<MealEntity>>

    // ---------------------------------------------------------
    // DAILY NUTRITION SUMMARY SUPPORT
    // ---------------------------------------------------------
    @Query("""
        SELECT 
            SUM(protein) AS totalProtein,
            SUM(fat) AS totalFat,
            SUM(carbs) AS totalCarbs,
            SUM(COALESCE(calories, 0)) AS totalCalories,
            SUM(COALESCE(sodium, 0)) AS totalSodium,
            SUM(COALESCE(cholesterol, 0)) AS totalCholesterol,
            SUM(COALESCE(fiber, 0)) AS totalFiber
        FROM meals
        WHERE date = :date
    """)
    suspend fun getDailyMacroTotals(date: String): DailyMacroTotals?
}

/** Helper projection for daily totals */
data class DailyMacroTotals(
    val totalProtein: Double?,
    val totalFat: Double?,
    val totalCarbs: Double?,
    val totalCalories: Double?,
    val totalSodium: Double?,
    val totalCholesterol: Double?,
    val totalFiber: Double?
)
