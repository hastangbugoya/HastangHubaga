package com.example.hastanghubaga.data.local.dao.meal

import androidx.room.*
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

    // IMPORTANT: date string must be "YYYY-MM-DD"
    @Transaction
    @Query("""
        SELECT * FROM meals 
        WHERE date(timestamp/1000,'unixepoch') = :date
    """)
    suspend fun getMealsForDate(date: String): List<MealJoinedRoom>


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
