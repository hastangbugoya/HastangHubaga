package com.example.hastanghubaga.data.local.dao.supplement

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.hastanghubaga.data.local.entity.supplement.SupplementEntity
import com.example.hastanghubaga.data.local.entity.supplement.SupplementWithIngredients
import com.example.hastanghubaga.data.local.models.SupplementJoinedRoom
import kotlinx.coroutines.flow.Flow

/**
 * DAO for the `supplements` table.
 *
 * Responsibilities:
 * - CRUD access to supplements
 * - Reactive and non-reactive queries
 * - Relationship resolution for:
 *   - supplements ↔ ingredients
 *   - supplements ↔ user settings
 *
 * NOTE:
 * - This DAO is persistence-only.
 * - Domain models are built in the repository layer.
 */
@Dao
interface SupplementEntityDao {

    /* ==================================================
     * INSERT / UPDATE / DELETE
     * ==================================================
     */

    /**
     * Insert or replace a single supplement.
     *
     * Table: supplements
     * Used by: setup flows, editors, migrations
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplement(supplement: SupplementEntity): Long

    /**
     * Insert or replace multiple supplements.
     *
     * Table: supplements
     * Used by: database seed / restore / bulk sync
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplements(list: List<SupplementEntity>)

    /**
     * Bulk insert (non-suspending).
     *
     * Table: supplements
     * Used by: database callbacks / prepopulation
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(entries: List<SupplementEntity>)

    /**
     * Update an existing supplement row.
     *
     * Table: supplements
     */
    @Update
    suspend fun updateSupplement(supplement: SupplementEntity)

    /**
     * Delete a supplement row.
     *
     * Table: supplements
     */
    @Delete
    suspend fun deleteSupplement(supplement: SupplementEntity)


    /* ==================================================
     * BASIC QUERIES (NO RELATIONS)
     * ==================================================
     */

    /**
     * Fetch a single supplement by ID.
     *
     * Table: supplements
     * Non-reactive, safe for background use.
     */
    @Query("SELECT * FROM supplements WHERE id = :id LIMIT 1")
    suspend fun getSupplementById(id: Long): SupplementEntity?

    /**
     * Fetch all supplements once.
     *
     * Table: supplements
     * Used by: workers, exports, one-off jobs
     */
    @Query("SELECT * FROM supplements")
    suspend fun getAllSupplementsOnce(): List<SupplementEntity>

    /**
     * Fetch all supplements (reactive).
     *
     * Table: supplements
     * Used by: UI lists
     */
    @Query("SELECT * FROM supplements ORDER BY name ASC")
    fun getAllSupplementsFlow(): Flow<List<SupplementEntity>>

    /**
     * Fetch all supplements (non-reactive).
     *
     * Table: supplements
     * Used by: workers / sync
     */
    @Query("SELECT * FROM supplements ORDER BY name ASC")
    suspend fun getAllSupplements(): List<SupplementEntity>

    /**
     * Fetch all active supplements (reactive).
     *
     * Table: supplements
     * Used by: Today timeline, alerts, widgets
     */
    @Query("SELECT * FROM supplements WHERE isActive = 1")
    fun getActiveSupplementsFlow(): Flow<List<SupplementEntity>>

    /**
     * Fetch all active supplements (reactive, alias).
     *
     * Table: supplements
     * Kept for semantic clarity.
     */
    @Query("SELECT * FROM supplements WHERE isActive = 1")
    fun getActiveSupplements(): Flow<List<SupplementEntity>>

    /**
     * Fetch active supplements ordered by offsetMinutes.
     *
     * Table: supplements
     * Used by: scheduling logic
     */
    @Query("""
        SELECT * FROM supplements
        WHERE isActive = 1
        ORDER BY offsetMinutes ASC
    """)
    suspend fun getActiveSupplementsOrderedByOffset(): List<SupplementEntity>

    /**
     * Search supplements by name (reactive).
     *
     * Table: supplements
     * Used by: autocomplete / search UI
     */
    @Query("""
        SELECT * FROM supplements
        WHERE name LIKE '%' || :query || '%'
        ORDER BY name ASC
    """)
    fun searchSupplements(query: String): Flow<List<SupplementEntity>>

    /**
     * Fetch all supplements ordered by ID.
     *
     * Table: supplements
     * Used by: migrations / diagnostics
     */
    @Query("SELECT * FROM supplements ORDER BY id ASC")
    suspend fun getAllOnce(): List<SupplementEntity>


    /* ==================================================
     * RELATIONS: SUPPLEMENTS ↔ INGREDIENTS
     * ==================================================
     */

    /**
     * Fetch a supplement with its ingredients.
     *
     * Tables:
     * - supplements
     * - supplement_ingredient_join
     * - ingredients
     *
     * Transaction ensures relational consistency.
     */
    @Transaction
    @Query("SELECT * FROM supplements WHERE id = :id")
    suspend fun getSupplementWithIngredients(id: Long): SupplementWithIngredients?

    /**
     * Fetch all supplements with ingredients.
     *
     * Tables:
     * - supplements
     * - supplement_ingredient_join
     * - ingredients
     *
     * Used by: editors, exports
     */
    @Transaction
    @Query("SELECT * FROM supplements ORDER BY name ASC")
    suspend fun getAllSupplementsWithIngredients(): List<SupplementWithIngredients>


    /* ==================================================
     * RELATIONS: SUPPLEMENTS ↔ USER SETTINGS
     * ==================================================
     */

    /**
     * Observe a single supplement entity (reactive).
     *
     * Table: supplements
     * Used by: detail screens
     */
    @Query("SELECT * FROM supplements WHERE id = :id")
    fun observeSupplementById(id: Long): Flow<SupplementEntity>

    /**
     * Observe supplement joined with user settings.
     *
     * Tables:
     * - supplements
     * - supplement_user_settings
     *
     * Used by: settings UI, detail screens
     */
    @Transaction
    @Query("SELECT * FROM supplements WHERE id = :id")
    fun observeSupplementWithSettings(id: Long): Flow<SupplementJoinedRoom?>

    /**
     * Fetch supplement joined with user settings (one-shot).
     *
     * Tables:
     * - supplements
     * - supplement_user_settings
     *
     * Used by: workers, background logic
     */
    @Transaction
    @Query("SELECT * FROM supplements WHERE id = :id")
    suspend fun getSupplementWithSettings(id: Long): SupplementJoinedRoom?
}