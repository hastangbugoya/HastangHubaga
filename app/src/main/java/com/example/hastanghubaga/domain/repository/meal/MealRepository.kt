package com.example.hastanghubaga.domain.repository.meal

import com.example.hastanghubaga.data.local.entity.meal.MealEntity
import com.example.hastanghubaga.data.local.entity.meal.MealNutritionEntity
import com.example.hastanghubaga.domain.model.meal.Meal
import com.example.hastanghubaga.domain.model.meal.MealNutrition
import com.example.hastanghubaga.domain.model.meal.NutritionInput
import kotlinx.coroutines.flow.Flow

/**
 * Repository contract for HH native meals.
 *
 * Meals are now moving to the same architecture as activities:
 *
 * - MealEntity = reusable template
 * - meal schedule tables = recurrence + timing rules
 * - future occurrence layer = date-specific planned instances
 * - current logging path = native HH actual meal capture
 *
 * Because of that, this repository should expose:
 *
 * - template-level reads/writes
 * - schedule save/delete operations
 * - existing native HH meal logging operations
 *
 * It should NOT expose old "template table filtered by date" semantics.
 */
interface MealRepository {

    // ------------------------------------------------------------------------
    // Template reads
    // ------------------------------------------------------------------------

    fun observeAll(): Flow<List<Meal>>

    fun observeActive(): Flow<List<Meal>>

    fun observeMeal(id: Long): Flow<Meal?>

    suspend fun getMealById(id: Long): Meal?

    suspend fun getAllOnce(): List<Meal>

    suspend fun getActiveOnce(): List<Meal>

    // ------------------------------------------------------------------------
    // Template writes
    // ------------------------------------------------------------------------

    /**
     * Creates or updates a meal template and its nutrition payload.
     *
     * Schedule persistence is handled separately so the reusable scheduling UI
     * can save template details and schedule details explicitly.
     */
    suspend fun upsertMeal(
        meal: MealEntity,
        nutrition: MealNutritionEntity
    ): Long

    suspend fun deleteMeal(meal: MealEntity)

    suspend fun deleteMealById(mealId: Long)

    // ------------------------------------------------------------------------
    // Schedule writes / reads
    // ------------------------------------------------------------------------

    /**
     * Removes any persisted schedule for the given meal template.
     */
    suspend fun clearSchedule(mealId: Long)

    /**
     * Whether the meal currently has any persisted schedule row.
     */
    suspend fun hasSchedule(mealId: Long): Boolean

    // ------------------------------------------------------------------------
    // Transitional native HH meal logging
    // ------------------------------------------------------------------------

    /**
     * Saves a native HH meal log.
     *
     * This remains while meals are still in the transitional model described in
     * the meal blueprint:
     * - native HH meals are editable/loggable
     * - imported AK meals remain read-only
     * - full occurrence-aware reconciliation is future work
     */
    suspend fun logMeal(
        mealId: Long?,
        timestampMillis: Long,
        notes: String?,
        nutrition: NutritionInput?
    )

    fun observeMealNutritionForDate(dateMillis: Long): Flow<List<MealNutrition>>
}