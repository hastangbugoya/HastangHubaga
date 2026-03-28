package com.example.hastanghubaga.data.local.dao.meal

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.hastanghubaga.data.local.entity.meal.AkImportedMealEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AkImportedMealDao {

    // -------------------------------
    // READ
    // -------------------------------

    @Query("""
        SELECT * FROM ak_imported_meals
        ORDER BY timestamp DESC
    """)
    fun observeAll(): Flow<List<AkImportedMealEntity>>

    @Query("""
        SELECT * FROM ak_imported_meals
        WHERE groupingKey = :groupingKey
        LIMIT 1
    """)
    suspend fun getByGroupingKey(groupingKey: String): AkImportedMealEntity?

    @Query("""
        SELECT * FROM ak_imported_meals
        WHERE logDateIso = :logDateIso
        ORDER BY timestamp ASC
    """)
    suspend fun getForDate(logDateIso: String): List<AkImportedMealEntity>

    @Query("""
        SELECT * FROM ak_imported_meals
        WHERE logDateIso = :logDateIso
        ORDER BY timestamp ASC
    """)
    fun observeForDate(logDateIso: String): Flow<List<AkImportedMealEntity>>

    // -------------------------------
    // WRITE
    // -------------------------------

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(entity: AkImportedMealEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplaceAll(entities: List<AkImportedMealEntity>)

    // -------------------------------
    // DELETE
    // -------------------------------

    @Query("""
        DELETE FROM ak_imported_meals
        WHERE groupingKey = :groupingKey
    """)
    suspend fun deleteByGroupingKey(groupingKey: String)

    @Query("""
        DELETE FROM ak_imported_meals
        WHERE logDateIso = :logDateIso
    """)
    suspend fun deleteForDate(logDateIso: String)

    @Query("""
        DELETE FROM ak_imported_meals
    """)
    suspend fun deleteAll()

    // -------------------------------
    // REBUILD HELPERS
    // -------------------------------

    /**
     * Replaces the entire imported-meal materialization for a given date.
     *
     * Intended usage:
     * - derive grouped meals from ak_imported_logs for one date
     * - wipe existing ak_imported_meals rows for that date
     * - insert the newly derived rows
     */
    suspend fun replaceForDate(
        logDateIso: String,
        meals: List<AkImportedMealEntity>
    ) {
        deleteForDate(logDateIso)
        if (meals.isNotEmpty()) {
            insertOrReplaceAll(meals)
        }
    }
}