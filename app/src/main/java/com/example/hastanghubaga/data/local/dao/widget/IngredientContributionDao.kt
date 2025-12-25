package com.example.hastanghubaga.data.local.dao.widget

import androidx.room.Dao
import androidx.room.Query
import com.example.hastanghubaga.domain.model.widget.DailyIngredientSummaryRow

@Dao
interface IngredientContributionDao {

    @Query("""
        SELECT
            c.ingredientId AS ingredientId,
            i.name AS name,
            i.unit AS unit,
            SUM(c.amount) AS totalAmount
        FROM ingredient_contributions c
        INNER JOIN ingredients i
            ON i.id = c.ingredientId
        WHERE c.consumedAt BETWEEN :start AND :end
        GROUP BY c.ingredientId
        ORDER BY i.name ASC
    """)
    suspend fun getDailySummary(
        start: Long,
        end: Long
    ): List<DailyIngredientSummaryRow>
}
