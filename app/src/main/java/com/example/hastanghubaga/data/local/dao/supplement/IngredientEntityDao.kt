package com.example.hastanghubaga.data.local.dao.supplement

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.hastanghubaga.data.local.entity.supplement.IngredientEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IngredientEntityDao {

    // Insert or update a single ingredient
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredient(ingredient: IngredientEntity): Long

    // Insert multiple ingredients (e.g., bulk load)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredients(ingredients: List<IngredientEntity>)

    // Update the entire ingredient row
    @Update
    suspend fun updateIngredient(ingredient: IngredientEntity)

    // Delete ingredient
    @Delete
    suspend fun deleteIngredient(ingredient: IngredientEntity)

    // Get ingredient by ID
    @Query("SELECT * FROM ingredients WHERE id = :id LIMIT 1")
    suspend fun getIngredientById(id: Long): IngredientEntity?

    // Lookup by exact name (unique index enforces 1 result)
    @Query("SELECT * FROM ingredients WHERE name = :name LIMIT 1")
    suspend fun getIngredientByName(name: String): IngredientEntity?

    // Reactive Flow for UI
    @Query("SELECT * FROM ingredients ORDER BY name ASC")
    fun getAllIngredientsFlow(): Flow<List<IngredientEntity>>

    // Non-reactive list (for workers, import/export)
    @Query("SELECT * FROM ingredients ORDER BY name ASC")
    suspend fun getAllIngredients(): List<IngredientEntity>

    // Search (for autocomplete UI)
    @Query("SELECT * FROM ingredients WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchIngredients(query: String): Flow<List<IngredientEntity>>

    // Update RDA only (without overwriting other fields)
    @Query(
        """
        UPDATE ingredients
        SET rdaValue = :rdaValue,
            rdaUnit = :rdaUnit
        WHERE id = :id
        """
    )
    suspend fun updateRda(id: Long, rdaValue: Double?, rdaUnit: String?)

    // Update UL (tolerable upper limit)
    @Query(
        """
        UPDATE ingredients
        SET upperLimitValue = :ulValue,
            upperLimitUnit = :ulUnit
        WHERE id = :id
        """
    )
    suspend fun updateUpperLimit(id: Long, ulValue: Double?, ulUnit: String?)

    // Update category only (vitamin, mineral, herb…)
    @Query("UPDATE ingredients SET category = :category WHERE id = :id")
    suspend fun updateCategory(id: Long, category: String?)

    // Delete all ingredients
    @Query("DELETE FROM ingredients")
    suspend fun clearAllIngredients()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredientsReturningIds(ingredients: List<IngredientEntity>): List<Long>

}