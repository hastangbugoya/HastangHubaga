package com.example.hastanghubaga.data.local.dao.nutrition

import androidx.room.Dao
import androidx.room.Query
import com.example.hastanghubaga.data.local.entity.user.NutrientGoalEntity

@Dao
interface NutrientGoalDao {
    @Query("""
        SELECT *
        FROM nutrient_goals
        WHERE planId = :planId
          AND ingredientId IN (:ingredientIds)
          AND isEnabled = 1
    """)
    suspend fun getGoalsForIngredients(
        planId: Long,
        ingredientIds: List<Long>
    ): List<NutrientGoalEntity>
}
