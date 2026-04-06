package com.example.hastanghubaga.data.local.dao.supplement

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.hastanghubaga.data.local.entity.supplement.SupplementIngredientEntity
import kotlinx.coroutines.flow.Flow

/**
 * Room DAO for supplement ↔ ingredient link rows.
 *
 * Each row represents one ingredient attached to one supplement, along with the
 * supplement-specific payload such as bottle-label display name, amount per
 * serving, and unit.
 *
 * Design rules:
 * - A supplement may have many linked ingredients
 * - An ingredient may appear in many supplements
 * - The same supplement/ingredient pair should exist at most once
 *   (enforced at the entity/schema level via a unique composite index)
 * - Editor save flows should be able to replace the full ingredient set for a
 *   supplement transactionally
 */
@Dao
interface SupplementIngredientDao {

    // ------------------------------------------------------------------------
    // Inserts / upserts
    // ------------------------------------------------------------------------

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLink(link: SupplementIngredientEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLinks(links: List<SupplementIngredientEntity>)

    // ------------------------------------------------------------------------
    // Reads
    // ------------------------------------------------------------------------

    @Query("""
        SELECT * FROM supplement_ingredients
        WHERE supplementId = :supplementId
        ORDER BY id ASC
    """)
    suspend fun getLinksForSupplement(supplementId: Long): List<SupplementIngredientEntity>

    @Query("""
        SELECT * FROM supplement_ingredients
        WHERE supplementId = :supplementId
        ORDER BY id ASC
    """)
    fun observeLinksForSupplement(supplementId: Long): Flow<List<SupplementIngredientEntity>>

    @Query("SELECT * FROM supplement_ingredients")
    suspend fun getAllLinks(): List<SupplementIngredientEntity>

    // ------------------------------------------------------------------------
    // Deletes
    // ------------------------------------------------------------------------

    @Query("""
        DELETE FROM supplement_ingredients
        WHERE supplementId = :supplementId
          AND ingredientId = :ingredientId
    """)
    suspend fun deleteLinkForSupplementIngredient(
        supplementId: Long,
        ingredientId: Long
    )

    @Query("DELETE FROM supplement_ingredients WHERE supplementId = :supplementId")
    suspend fun deleteLinksForSupplement(supplementId: Long)

    // ------------------------------------------------------------------------
    // Transactional helpers
    // ------------------------------------------------------------------------

    /**
     * Replaces the full ingredient set for a supplement.
     *
     * This is the safest save primitive for the supplement editor:
     * - clear all existing linked ingredients for the supplement
     * - insert the new set exactly as authored by the user
     *
     * This prevents stale links from surviving after the user unchecks items.
     */
    @Transaction
    suspend fun replaceLinksForSupplement(
        supplementId: Long,
        links: List<SupplementIngredientEntity>
    ) {
        deleteLinksForSupplement(supplementId)

        if (links.isNotEmpty()) {
            insertLinks(
                links.map { it.copy(id = 0L, supplementId = supplementId) }
            )
        }
    }
}