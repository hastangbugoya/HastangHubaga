package com.example.hastanghubaga.data.local.dao.nutrition

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.hastanghubaga.data.local.entity.user.NutrientGoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NutrientGoalDao {

    // ---------------------------------------------------------
    // INSERT + UPDATE + DELETE
    // ---------------------------------------------------------

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(goal: NutrientGoalEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(goals: List<NutrientGoalEntity>): List<Long>

    @Update
    suspend fun update(goal: NutrientGoalEntity)

    @Update
    suspend fun updateAll(goals: List<NutrientGoalEntity>)

    @Delete
    suspend fun delete(goal: NutrientGoalEntity)

    @Delete
    suspend fun deleteAll(goals: List<NutrientGoalEntity>)

    // ---------------------------------------------------------
    // SINGLE ROW LOOKUPS
    // ---------------------------------------------------------

    @Query("""
        SELECT *
        FROM nutrition_plan_goals
        WHERE id = :goalId
        LIMIT 1
    """)
    suspend fun getGoalById(goalId: Long): NutrientGoalEntity?

    @Query("""
        SELECT *
        FROM nutrition_plan_goals
        WHERE planId = :planId
          AND nutrientKey = :nutrientKey
        LIMIT 1
    """)
    suspend fun getGoalByPlanIdAndNutrientKey(
        planId: Long,
        nutrientKey: String
    ): NutrientGoalEntity?

    // ---------------------------------------------------------
    // PLAN-SCOPED QUERIES
    // ---------------------------------------------------------

    @Query("""
        SELECT *
        FROM nutrition_plan_goals
        WHERE planId = :planId
        ORDER BY nutrientKey ASC, id ASC
    """)
    suspend fun getGoalsForPlan(planId: Long): List<NutrientGoalEntity>

    @Query("""
        SELECT *
        FROM nutrition_plan_goals
        WHERE planId = :planId
        ORDER BY nutrientKey ASC, id ASC
    """)
    fun observeGoalsForPlan(planId: Long): Flow<List<NutrientGoalEntity>>

    @Query("""
        SELECT *
        FROM nutrition_plan_goals
        WHERE planId IN (:planIds)
        ORDER BY planId ASC, nutrientKey ASC, id ASC
    """)
    suspend fun getGoalsForPlans(planIds: List<Long>): List<NutrientGoalEntity>

    // ---------------------------------------------------------
    // FILTERS
    // ---------------------------------------------------------

    @Query("""
        SELECT *
        FROM nutrition_plan_goals
        WHERE nutrientKey = :nutrientKey
        ORDER BY planId ASC, id ASC
    """)
    suspend fun getGoalsByNutrientKey(nutrientKey: String): List<NutrientGoalEntity>

    @Query("""
        SELECT *
        FROM nutrition_plan_goals
        WHERE planId = :planId
          AND nutrientKey IN (:nutrientKeys)
        ORDER BY nutrientKey ASC, id ASC
    """)
    suspend fun getGoalsForPlanByNutrientKeys(
        planId: Long,
        nutrientKeys: List<String>
    ): List<NutrientGoalEntity>

    // ---------------------------------------------------------
    // DELETE HELPERS
    // ---------------------------------------------------------

    @Query("""
        DELETE FROM nutrition_plan_goals
        WHERE planId = :planId
    """)
    suspend fun deleteGoalsForPlan(planId: Long)

    @Query("""
        DELETE FROM nutrition_plan_goals
        WHERE planId = :planId
          AND nutrientKey NOT IN (:nutrientKeys)
    """)
    suspend fun deleteGoalsForPlanExcept(
        planId: Long,
        nutrientKeys: List<String>
    )

    @Query("""
        DELETE FROM nutrition_plan_goals
        WHERE planId = :planId
          AND nutrientKey = :nutrientKey
    """)
    suspend fun deleteGoalByPlanIdAndNutrientKey(
        planId: Long,
        nutrientKey: String
    )
}