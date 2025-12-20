package com.example.hastanghubaga.data.local.dao.meal

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.hastanghubaga.data.local.entity.meal.MealEntity
import com.example.hastanghubaga.data.local.entity.meal.MealNutritionEntity
import com.example.hastanghubaga.data.local.entity.meal.MealType
import com.example.hastanghubaga.data.local.models.MealJoinedRoom
import kotlinx.coroutines.flow.Flow

@Dao
interface MealEntityDao {

    // -------------------------------
    // READ
    // -------------------------------

    @Transaction
    @Query("SELECT * FROM meals ORDER BY timestamp DESC")
    fun observeAllMeals(): Flow<List<MealJoinedRoom>>

    @Transaction
    @Query("SELECT * FROM meals WHERE id = :id")
    fun observeMeal(id: Long): Flow<MealJoinedRoom?>

    @Transaction
    @Query("""
            SELECT * FROM meals
            WHERE timestamp BETWEEN :start AND :end
            ORDER BY timestamp ASC
        """)
    suspend fun getMealsForDayOnce(
        start: Long,
        end: Long
    ): List<MealJoinedRoom>

    @Transaction
    @Query("""
    SELECT * FROM meals
    WHERE timestamp BETWEEN :start AND :end
    ORDER BY timestamp ASC
""")
    fun observeMealsForDay(
        start: Long,
        end: Long
    ): Flow<List<MealJoinedRoom>>

    // -------------------------------
    // INSERT
    // -------------------------------

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeal(meal: MealEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNutrition(nutrition: MealNutritionEntity)


    // -------------------------------
    // DELETE
    // -------------------------------

    @Delete
    suspend fun deleteMeal(meal: MealEntity)   // valid, keeps it for convenience

    @Query("DELETE FROM meals WHERE id = :mealId")
    suspend fun deleteMealById(mealId: Long)   // used by repository

    @Query("DELETE FROM meal_nutrition WHERE mealId = :mealId")
    suspend fun deleteNutrition(mealId: Long)


    // -------------------------------
    // FILTER
    // -------------------------------

    @Query("SELECT * FROM meals WHERE type = :type")
    suspend fun getMealsByType(type: MealType): List<MealEntity>

}
