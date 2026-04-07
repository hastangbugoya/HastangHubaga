package com.example.hastanghubaga.data.local.dao.nutrition

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.hastanghubaga.data.local.entity.user.UserNutritionPlanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NutritionPlanEntityDao {

    // ---------------------------------------------------------
    // INSERT + UPDATE + DELETE
    // ---------------------------------------------------------

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(plan: UserNutritionPlanEntity): Long

    @Update
    suspend fun update(plan: UserNutritionPlanEntity)

    @Delete
    suspend fun delete(plan: UserNutritionPlanEntity)

    // ---------------------------------------------------------
    // SINGLE PLAN LOOKUPS
    // ---------------------------------------------------------

    @Query("""
        SELECT *
        FROM nutrition_plan
        WHERE id = :planId
        LIMIT 1
    """)
    suspend fun getPlanById(planId: Long): UserNutritionPlanEntity?

    @Query("""
        SELECT *
        FROM nutrition_plan
        WHERE id = :planId
        LIMIT 1
    """)
    fun observePlanById(planId: Long): Flow<UserNutritionPlanEntity?>

    // ---------------------------------------------------------
    // LIST / OBSERVE ALL
    // ---------------------------------------------------------

    @Query("""
        SELECT *
        FROM nutrition_plan
        ORDER BY updatedAt DESC, id DESC
    """)
    suspend fun getAllPlans(): List<UserNutritionPlanEntity>

    @Query("""
        SELECT *
        FROM nutrition_plan
        ORDER BY updatedAt DESC, id DESC
    """)
    fun observeAllPlans(): Flow<List<UserNutritionPlanEntity>>

    // ---------------------------------------------------------
    // ACTIVE PLANS
    // ---------------------------------------------------------

    @Query("""
        SELECT *
        FROM nutrition_plan
        WHERE isActive = 1
        ORDER BY updatedAt DESC, id DESC
    """)
    suspend fun getActivePlans(): List<UserNutritionPlanEntity>

    @Query("""
        SELECT *
        FROM nutrition_plan
        WHERE isActive = 1
        ORDER BY updatedAt DESC, id DESC
    """)
    fun observeActivePlans(): Flow<List<UserNutritionPlanEntity>>

    // ---------------------------------------------------------
    // FILTERS
    // ---------------------------------------------------------

    @Query("""
        SELECT *
        FROM nutrition_plan
        WHERE type = :type
        ORDER BY updatedAt DESC, id DESC
    """)
    suspend fun getPlansByType(type: String): List<UserNutritionPlanEntity>

    @Query("""
        SELECT *
        FROM nutrition_plan
        WHERE sourceType = :sourceType
        ORDER BY updatedAt DESC, id DESC
    """)
    suspend fun getPlansBySourceType(sourceType: String): List<UserNutritionPlanEntity>

    @Query("""
        SELECT *
        FROM nutrition_plan
        WHERE sourceType = :sourceType
          AND sourcePlanId = :sourcePlanId
        LIMIT 1
    """)
    suspend fun getPlanBySource(sourceType: String, sourcePlanId: String): UserNutritionPlanEntity?

    // ---------------------------------------------------------
    // ACTIVE STATE
    // ---------------------------------------------------------

    @Query("""
        UPDATE nutrition_plan
        SET isActive = :isActive,
            updatedAt = :updatedAt
        WHERE id = :planId
    """)
    suspend fun setPlanActiveState(
        planId: Long,
        isActive: Boolean,
        updatedAt: Long
    )

    // ---------------------------------------------------------
    // TIMELINE / DATE VALIDITY HELPERS
    // ---------------------------------------------------------

    @Query("""
        SELECT *
        FROM nutrition_plan
        WHERE startDate <= :dateMillis
          AND (endDate IS NULL OR endDate >= :dateMillis)
        ORDER BY isActive DESC, updatedAt DESC, id DESC
    """)
    suspend fun getPlansEffectiveOn(dateMillis: Long): List<UserNutritionPlanEntity>

    @Query("""
        SELECT *
        FROM nutrition_plan
        WHERE isActive = 1
          AND startDate <= :dateMillis
          AND (endDate IS NULL OR endDate >= :dateMillis)
        ORDER BY updatedAt DESC, id DESC
    """)
    suspend fun getActivePlansEffectiveOn(dateMillis: Long): List<UserNutritionPlanEntity>
}