package com.example.hastanghubaga.domain.repository.nutrition


import com.example.hastanghubaga.data.local.entity.user.UserNutritionGoalsEntity
import com.example.hastanghubaga.domain.model.nutrition.NutritionGoalType
import com.example.hastanghubaga.domain.model.nutrition.NutritionGoal
import kotlinx.coroutines.flow.Flow

interface NutritionGoalsRepository {

    /** Observe all saved nutrition goals */
    fun observeAll(): Flow<List<NutritionGoal>>

    /** Observe the user's currently active goal */
    fun observeActive(): Flow<NutritionGoal?>

    /** Get all goals once (non-reactive) */
    suspend fun getAll(): List<NutritionGoal>

    /** Get the active goal once (non-reactive) */
    suspend fun getActive(): NutritionGoal?

    /** Insert or update a goal */
    suspend fun upsert(goal: NutritionGoal): Long

    /** Delete a goal */
    suspend fun delete(goal: NutritionGoal)

    /** Set a specific goal as active (auto-disables others) */
    suspend fun setActive(goalId: Long)

    /** Get all goals by type (bulking, cutting, etc.) */
    suspend fun getByType(type: NutritionGoalType): List<NutritionGoal>

    fun observeActiveGoal(): Flow<UserNutritionGoalsEntity?>
}