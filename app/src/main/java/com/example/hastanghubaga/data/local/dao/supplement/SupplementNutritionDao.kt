package com.example.hastanghubaga.data.local.dao.supplement

import androidx.room.Dao
import androidx.room.Query
import com.example.hastanghubaga.domain.model.supplement.SupplementLogNutrientRow
import com.example.hastanghubaga.domain.model.supplement.SupplementNutritionRow
import kotlinx.coroutines.flow.Flow

data class SupplementNutritionAtTimeRow(
    val timestamp: Long,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val calories: Double,
    val sodium: Double,
    val cholesterol: Double,
    val fiber: Double
)
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

    @Query(
        """
        SELECT
            l.timestamp AS timestamp,

            SUM(CASE WHEN LOWER(i.name) = 'protein'
                THEN si.amountPerServing * (l.actualServingTaken / s.recommendedServingSize)
                ELSE 0 END) AS protein,

            SUM(CASE WHEN LOWER(i.name) IN ('carbs','carb','carbohydrates')
                THEN si.amountPerServing * (l.actualServingTaken / s.recommendedServingSize)
                ELSE 0 END) AS carbs,

            SUM(CASE WHEN LOWER(i.name) = 'fat'
                THEN si.amountPerServing * (l.actualServingTaken / s.recommendedServingSize)
                ELSE 0 END) AS fat,

            SUM(CASE WHEN LOWER(i.name) IN ('calories','kcal')
                THEN si.amountPerServing * (l.actualServingTaken / s.recommendedServingSize)
                ELSE 0 END) AS calories,

            SUM(CASE WHEN LOWER(i.name) IN ('sodium','salt')
                THEN si.amountPerServing * (l.actualServingTaken / s.recommendedServingSize)
                ELSE 0 END) AS sodium,

            SUM(CASE WHEN LOWER(i.name) IN ('cholesterol')
                THEN si.amountPerServing * (l.actualServingTaken / s.recommendedServingSize)
                ELSE 0 END) AS cholesterol,

            SUM(CASE WHEN LOWER(i.name) IN ('fiber','fibre')
                THEN si.amountPerServing * (l.actualServingTaken / s.recommendedServingSize)
                ELSE 0 END) AS fiber

        FROM supplement_daily_log l
        INNER JOIN supplements s ON s.id = l.supplementId
        INNER JOIN supplement_ingredients si ON si.supplementId = s.id
        INNER JOIN ingredients i ON i.id = si.ingredientId

        WHERE l.timestamp >= :startMillis AND l.timestamp < :endMillis

        GROUP BY l.id
        ORDER BY l.timestamp ASC
        """
    )
    fun observeSupplementNutritionInRange(
        startMillis: Long,
        endMillis: Long
    ): Flow<List<SupplementNutritionAtTimeRow>>




}