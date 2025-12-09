package com.example.hastanghubaga.data.local.dao.user

import androidx.room.*
import com.example.hastanghubaga.domain.model.nutrition.NutritionGoalType
import com.example.hastanghubaga.data.local.entity.user.UserNutritionGoalsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserNutritionGoalsEntityDao {

    // ---------------------------------------------------------
    // INSERT + UPDATE
    // ---------------------------------------------------------
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(goal: UserNutritionGoalsEntity): Long

    @Update
    suspend fun update(goal: UserNutritionGoalsEntity)

    @Delete
    suspend fun delete(goal: UserNutritionGoalsEntity)

    // ---------------------------------------------------------
    // QUERY ALL
    // ---------------------------------------------------------
    @Query("SELECT * FROM nutrition_goals ORDER BY id DESC")
    fun observeAllGoals(): Flow<List<UserNutritionGoalsEntity>>

    @Query("SELECT * FROM nutrition_goals ORDER BY id DESC")
    suspend fun getAllGoals(): List<UserNutritionGoalsEntity>

    // ---------------------------------------------------------
    // ACTIVE GOAL
    // ---------------------------------------------------------
    @Query("SELECT * FROM nutrition_goals WHERE isActive = 1 LIMIT 1")
    fun observeActiveGoal(): Flow<UserNutritionGoalsEntity?>

    @Query("SELECT * FROM nutrition_goals WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveGoal(): UserNutritionGoalsEntity?

    // Make a goal active (only one active at a time)
    @Query("UPDATE nutrition_goals SET isActive = 0")
    suspend fun clearActiveGoal()

    @Query("UPDATE nutrition_goals SET isActive = 1 WHERE id = :goalId")
    suspend fun setActiveGoal(goalId: Long)

    suspend fun activateGoal(goalId: Long) {
        clearActiveGoal()
        setActiveGoal(goalId)
    }

    // ---------------------------------------------------------
    // Filter by goal type
    // ---------------------------------------------------------
    @Query("SELECT * FROM nutrition_goals WHERE type = :type")
    suspend fun getGoalsByType(type: NutritionGoalType): List<UserNutritionGoalsEntity>
}
