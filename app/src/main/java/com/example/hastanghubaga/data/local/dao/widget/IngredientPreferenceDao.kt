package com.example.hastanghubaga.data.local.dao.widget

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.hastanghubaga.data.local.entity.widget.IngredientPreferenceEntity

@Dao
interface IngredientPreferenceDao {

    @Query("SELECT * FROM ingredient_preferences")
    suspend fun getAll(): List<IngredientPreferenceEntity>

    @Query("SELECT * FROM ingredient_preferences WHERE ingredientId IN (:ids)")
    suspend fun getForIngredientIds(
        ids: List<Long>
    ): List<IngredientPreferenceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(preference: IngredientPreferenceEntity)
}
