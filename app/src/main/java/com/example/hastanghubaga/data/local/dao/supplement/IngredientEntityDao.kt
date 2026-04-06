package com.example.hastanghubaga.data.local.dao.supplement

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.hastanghubaga.data.local.entity.supplement.IngredientEntity
import com.example.hastanghubaga.data.local.entity.supplement.IngredientUnit
import kotlinx.coroutines.flow.Flow

/**
 * Room DAO for canonical ingredient master data.
 *
 * This table stores reusable ingredient definitions that can later be linked to
 * one or more supplements through SupplementIngredientEntity.
 *
 * Design rules:
 * - Ingredient CRUD operates on canonical ingredient records only
 * - Supplement-specific amounts/labels do NOT belong here
 * - UI should generally observe ingredients ordered by name
 */
@Dao
interface IngredientEntityDao {

    // ------------------------------------------------------------------------
    // Inserts / upserts
    // ------------------------------------------------------------------------

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredient(ingredient: IngredientEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredients(ingredients: List<IngredientEntity>): List<Long>

    @Update
    suspend fun updateIngredient(ingredient: IngredientEntity)

    @Delete
    suspend fun deleteIngredient(ingredient: IngredientEntity)

    // ------------------------------------------------------------------------
    // Reads
    // ------------------------------------------------------------------------

    @Query("SELECT * FROM ingredients WHERE id = :id LIMIT 1")
    suspend fun getIngredientById(id: Long): IngredientEntity?

    @Query("SELECT * FROM ingredients WHERE name = :name LIMIT 1")
    suspend fun getIngredientByName(name: String): IngredientEntity?

    @Query("SELECT * FROM ingredients ORDER BY name ASC")
    fun observeAllIngredients(): Flow<List<IngredientEntity>>

    @Query("SELECT * FROM ingredients ORDER BY name ASC")
    suspend fun getAllIngredients(): List<IngredientEntity>

    @Query("""
        SELECT * FROM ingredients
        WHERE name LIKE '%' || :query || '%'
        ORDER BY name ASC
    """)
    fun searchIngredients(query: String): Flow<List<IngredientEntity>>

    // ------------------------------------------------------------------------
    // Focused field updates
    // ------------------------------------------------------------------------

    @Query(
        """
        UPDATE ingredients
        SET rdaValue = :rdaValue,
            rdaUnit = :rdaUnit
        WHERE id = :id
        """
    )
    suspend fun updateRda(
        id: Long,
        rdaValue: Double?,
        rdaUnit: IngredientUnit?
    )

    @Query(
        """
        UPDATE ingredients
        SET upperLimitValue = :upperLimitValue,
            upperLimitUnit = :upperLimitUnit
        WHERE id = :id
        """
    )
    suspend fun updateUpperLimit(
        id: Long,
        upperLimitValue: Double?,
        upperLimitUnit: IngredientUnit?
    )

    @Query("UPDATE ingredients SET category = :category WHERE id = :id")
    suspend fun updateCategory(
        id: Long,
        category: String?
    )

    // ------------------------------------------------------------------------
    // Maintenance
    // ------------------------------------------------------------------------

    @Query("DELETE FROM ingredients")
    suspend fun clearAllIngredients()
}