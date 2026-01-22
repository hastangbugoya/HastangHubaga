package com.example.hastanghubaga.data.local.dao.meal

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.hastanghubaga.data.local.entity.meal.MealNutritionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MealNutritionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNutrition(nutrition: MealNutritionEntity)

    @Query("SELECT * FROM meal_nutrition WHERE mealId = :mealId LIMIT 1")
    suspend fun getNutritionForMeal(mealId: Long): MealNutritionEntity?

    @Query("DELETE FROM meal_nutrition WHERE mealId = :mealId")
    suspend fun deleteNutrition(mealId: Long)

    @Query(
        """
        SELECT n.*
        FROM meal_nutrition n
        INNER JOIN meals m ON m.id = n.mealId
        WHERE m.timestamp >= :startMillis AND m.timestamp < :endMillis
        ORDER BY m.timestamp ASC
        """
    )
    fun observeNutritionForMealsInRange(
        startMillis: Long,
        endMillis: Long
    ): Flow<List<MealNutritionEntity>>
}
