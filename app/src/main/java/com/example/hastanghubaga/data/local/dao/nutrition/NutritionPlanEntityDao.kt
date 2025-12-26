package com.example.hastanghubaga.data.local.dao.nutrition

import androidx.room.Dao
import androidx.room.Query
import com.example.hastanghubaga.data.local.entity.user.UserNutritionPlanEntity

@Dao
interface NutritionPlanEntityDao {

    @Query("""
        SELECT *
        FROM nutrition_plan
        WHERE isActive = 1
        LIMIT 1
    """)
    suspend fun getActivePlan(): UserNutritionPlanEntity?
}
