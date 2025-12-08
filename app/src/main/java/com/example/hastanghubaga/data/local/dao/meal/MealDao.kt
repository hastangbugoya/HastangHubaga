package com.example.hastanghubaga.data.local.dao.meal

import androidx.room.*
import com.example.hastanghubaga.data.local.entity.meal.MealEntity
import com.example.hastanghubaga.data.local.entity.meal.MealNutritionEntity
import com.example.hastanghubaga.data.local.models.MealJoinedRoom
import kotlinx.coroutines.flow.Flow

@Dao
interface MealDao {

    @Transaction
    @Query("SELECT * FROM meals ORDER BY timestamp DESC")
    fun observeAllMeals(): Flow<List<MealJoinedRoom>>

    @Transaction
    @Query("SELECT * FROM meals WHERE id = :id")
    fun observeMeal(id: Long): Flow<MealJoinedRoom?>

    @Transaction
    @Query("SELECT * FROM meals WHERE date(timestamp/1000,'unixepoch') = :date")
    suspend fun getMealsForDate(date: String): List<MealJoinedRoom>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeal(meal: MealEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNutrition(nutrition: MealNutritionEntity)

    @Delete
    suspend fun deleteMeal(meal: MealEntity)

    @Query("DELETE FROM meal_nutrition WHERE mealId = :mealId")
    suspend fun deleteNutrition(mealId: Long)
}
