package com.example.hastanghubaga.data.local.dao.supplement

import androidx.room.Dao
import androidx.room.Query
import com.example.hastanghubaga.domain.model.supplement.SupplementLogNutrientRow
import com.example.hastanghubaga.domain.model.supplement.SupplementNutritionRow
import kotlinx.coroutines.flow.Flow

@Dao
interface SupplementNutritionDao {

    @Query(
        """
        SELECT
            l.id AS logId,
            i.name AS nutrientName,
            (si.amountPerServing * (l.actualServingTaken / s.recommendedServingSize)) AS amount
        FROM supplement_daily_log l
        INNER JOIN supplements s ON s.id = l.supplementId
        INNER JOIN supplement_ingredients si ON si.supplementId = s.id
        INNER JOIN ingredients i ON i.id = si.ingredientId
        WHERE l.timestamp >= :startMillis AND l.timestamp < :endMillis
        """
    )
    fun observeSupplementLogNutrientsInRange(
        startMillis: Long,
        endMillis: Long
    ): Flow<List<SupplementLogNutrientRow>>
}