package com.example.hastanghubaga.data.local.dao.nutrition

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.hastanghubaga.data.local.entity.user.UserNutritionPlanSuccessCriteriaEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for nutrition plan success-basis selections.
 *
 * This table is intentionally separate from nutrient goals.
 *
 * Why:
 * - a plan may track many nutrients
 * - only some may count toward daily "success"
 * - some plans may use successMode = NONE or ALL and never need these rows
 *
 * This DAO should mainly be used when the parent plan uses:
 * - CUSTOM_SELECTED_GOALS
 *
 * Future AI/dev note:
 * - do not assume every nutrient goal belongs in this table
 * - do not use this table unless successMode requires it
 * - delete/replace-per-plan is an acceptable first-pass strategy, similar to
 *   how nutrition_plan_goals are currently saved
 */
@Dao
interface NutritionPlanSuccessCriteriaDao {

    // ---------------------------------------------------------
    // INSERT + UPDATE + DELETE
    // ---------------------------------------------------------

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(
        criteria: UserNutritionPlanSuccessCriteriaEntity
    ): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(
        criteria: List<UserNutritionPlanSuccessCriteriaEntity>
    ): List<Long>

    @Update
    suspend fun update(
        criteria: UserNutritionPlanSuccessCriteriaEntity
    )

    @Update
    suspend fun updateAll(
        criteria: List<UserNutritionPlanSuccessCriteriaEntity>
    )

    @Delete
    suspend fun delete(
        criteria: UserNutritionPlanSuccessCriteriaEntity
    )

    @Delete
    suspend fun deleteAll(
        criteria: List<UserNutritionPlanSuccessCriteriaEntity>
    )

    // ---------------------------------------------------------
    // SINGLE ROW LOOKUPS
    // ---------------------------------------------------------

    @Query(
        """
        SELECT *
        FROM nutrition_plan_success_criteria
        WHERE id = :criteriaId
        LIMIT 1
        """
    )
    suspend fun getById(
        criteriaId: Long
    ): UserNutritionPlanSuccessCriteriaEntity?

    @Query(
        """
        SELECT *
        FROM nutrition_plan_success_criteria
        WHERE planId = :planId
          AND nutrientKey = :nutrientKey
        LIMIT 1
        """
    )
    suspend fun getByPlanIdAndNutrientKey(
        planId: Long,
        nutrientKey: String
    ): UserNutritionPlanSuccessCriteriaEntity?

    // ---------------------------------------------------------
    // PLAN-SCOPED QUERIES
    // ---------------------------------------------------------

    @Query(
        """
        SELECT *
        FROM nutrition_plan_success_criteria
        WHERE planId = :planId
        ORDER BY nutrientKey ASC, id ASC
        """
    )
    suspend fun getForPlan(
        planId: Long
    ): List<UserNutritionPlanSuccessCriteriaEntity>

    @Query(
        """
        SELECT *
        FROM nutrition_plan_success_criteria
        WHERE planId = :planId
        ORDER BY nutrientKey ASC, id ASC
        """
    )
    fun observeForPlan(
        planId: Long
    ): Flow<List<UserNutritionPlanSuccessCriteriaEntity>>

    @Query(
        """
        SELECT *
        FROM nutrition_plan_success_criteria
        WHERE planId IN (:planIds)
        ORDER BY planId ASC, nutrientKey ASC, id ASC
        """
    )
    suspend fun getForPlans(
        planIds: List<Long>
    ): List<UserNutritionPlanSuccessCriteriaEntity>

    // ---------------------------------------------------------
    // FILTERS
    // ---------------------------------------------------------

    @Query(
        """
        SELECT *
        FROM nutrition_plan_success_criteria
        WHERE nutrientKey = :nutrientKey
        ORDER BY planId ASC, id ASC
        """
    )
    suspend fun getByNutrientKey(
        nutrientKey: String
    ): List<UserNutritionPlanSuccessCriteriaEntity>

    // ---------------------------------------------------------
    // DELETE HELPERS
    // ---------------------------------------------------------

    @Query(
        """
        DELETE FROM nutrition_plan_success_criteria
        WHERE planId = :planId
        """
    )
    suspend fun deleteForPlan(
        planId: Long
    )

    @Query(
        """
        DELETE FROM nutrition_plan_success_criteria
        WHERE planId = :planId
          AND nutrientKey NOT IN (:nutrientKeys)
        """
    )
    suspend fun deleteForPlanExcept(
        planId: Long,
        nutrientKeys: List<String>
    )

    @Query(
        """
        DELETE FROM nutrition_plan_success_criteria
        WHERE planId = :planId
          AND nutrientKey = :nutrientKey
        """
    )
    suspend fun deleteByPlanIdAndNutrientKey(
        planId: Long,
        nutrientKey: String
    )
}