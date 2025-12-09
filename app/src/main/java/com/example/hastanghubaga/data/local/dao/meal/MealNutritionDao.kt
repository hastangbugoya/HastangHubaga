package com.example.hastanghubaga.data.local.dao.meal

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.hastanghubaga.data.local.entity.meal.MealNutritionEntity

@Dao
interface MealNutritionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNutrition(nutrition: MealNutritionEntity)

    @Query("SELECT * FROM meal_nutrition WHERE mealId = :mealId LIMIT 1")
    suspend fun getNutritionForMeal(mealId: Long): MealNutritionEntity?

    @Query("DELETE FROM meal_nutrition WHERE mealId = :mealId")
    suspend fun deleteNutrition(mealId: Long)
}
