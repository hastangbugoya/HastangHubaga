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

/**
 * DAO for reusable meal templates.
 *
 * Important architectural shift:
 * meals are no longer stored as point-in-time timestamped events.
 * They now behave like activity templates:
 *
 * - MealEntity = reusable template
 * - MealScheduleEntity + child timing rows = scheduling rules
 * - future occurrence/log layers determine date-specific appearance
 *
 * That means this DAO should NOT perform day-range filtering against meals.
 * Date-specific evaluation belongs to scheduling / occurrence logic, not the
 * meal template table itself.
 */
@Dao
interface MealEntityDao {

    // ------------------------------------------------------------------------
    // Reads
    // ------------------------------------------------------------------------

    @Transaction
    @Query("SELECT * FROM meals ORDER BY name ASC, id ASC")
    fun observeAllMeals(): Flow<List<MealJoinedRoom>>

    @Transaction
    @Query("SELECT * FROM meals WHERE isActive = 1 ORDER BY name ASC, id ASC")
    fun observeActiveMeals(): Flow<List<MealJoinedRoom>>

    @Transaction
    @Query("SELECT * FROM meals WHERE id = :id")
    fun observeMeal(id: Long): Flow<MealJoinedRoom?>

    @Query("SELECT * FROM meals WHERE id = :id")
    suspend fun getMealByIdOnce(id: Long): MealEntity?

    @Transaction
    @Query("SELECT * FROM meals ORDER BY name ASC, id ASC")
    suspend fun getAllMealsOnce(): List<MealJoinedRoom>

    @Transaction
    @Query("SELECT * FROM meals WHERE isActive = 1 ORDER BY name ASC, id ASC")
    suspend fun getActiveMealsOnce(): List<MealJoinedRoom>

    @Query("SELECT * FROM meals WHERE type = :type ORDER BY name ASC, id ASC")
    suspend fun getMealsByType(type: MealType): List<MealEntity>

    @Query("SELECT * FROM meals WHERE isActive = 1 AND type = :type ORDER BY name ASC, id ASC")
    suspend fun getActiveMealsByType(type: MealType): List<MealEntity>

    // ------------------------------------------------------------------------
    // Writes
    // ------------------------------------------------------------------------

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeal(meal: MealEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNutrition(nutrition: MealNutritionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMeal(meal: MealEntity): Long

    @Delete
    suspend fun deleteMeal(meal: MealEntity)

    @Query("DELETE FROM meals WHERE id = :mealId")
    suspend fun deleteMealById(mealId: Long)

    @Query("DELETE FROM meal_nutrition WHERE mealId = :mealId")
    suspend fun deleteNutrition(mealId: Long)
}