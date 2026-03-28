package com.example.hastanghubaga.data.local.dao.meal

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.hastanghubaga.data.local.entity.meal.AkImportedLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AkImportedLogDao {

    // -------------------------------
    // READ
    // -------------------------------

    @Query("""
        SELECT * FROM ak_imported_logs
        ORDER BY timestamp DESC
    """)
    fun observeAll(): Flow<List<AkImportedLogEntity>>

    @Query("""
        SELECT * FROM ak_imported_logs
        WHERE stableId = :stableId
        LIMIT 1
    """)
    suspend fun getByStableId(stableId: String): AkImportedLogEntity?

    @Query("""
        SELECT * FROM ak_imported_logs
        WHERE logDateIso = :logDateIso
        ORDER BY timestamp ASC
    """)
    suspend fun getForDate(logDateIso: String): List<AkImportedLogEntity>

    @Query("""
        SELECT * FROM ak_imported_logs
        WHERE logDateIso = :logDateIso
        ORDER BY timestamp ASC
    """)
    fun observeForDate(logDateIso: String): Flow<List<AkImportedLogEntity>>

    @Query("""
        SELECT * FROM ak_imported_logs
        ORDER BY timestamp DESC
        LIMIT :limit
    """)
    suspend fun getRecent(limit: Int = 200): List<AkImportedLogEntity>

    // -------------------------------
    // WRITE
    // -------------------------------

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(entity: AkImportedLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplaceAll(entities: List<AkImportedLogEntity>)

    // -------------------------------
    // DELETE
    // -------------------------------

    @Query("""
        DELETE FROM ak_imported_logs
        WHERE stableId = :stableId
    """)
    suspend fun deleteByStableId(stableId: String)

    @Query("""
        DELETE FROM ak_imported_logs
    """)
    suspend fun deleteAll()

    @Query("""
        DELETE FROM ak_imported_logs
        WHERE logDateIso = :logDateIso
    """)
    suspend fun deleteForDate(logDateIso: String)

    // -------------------------------
    // UPSERT HELPERS
    // -------------------------------

    /**
     * Inserts [incoming] if absent, or replaces the existing row only when
     * [incoming.modifiedAt] is newer than the stored row.
     *
     * @return true when the DB changed, false when the incoming row was ignored.
     */
    @Transaction
    suspend fun upsertIfNewer(incoming: AkImportedLogEntity): Boolean {
        val existing = getByStableId(incoming.stableId)
        return when {
            existing == null -> {
                insertOrReplace(incoming)
                true
            }

            incoming.modifiedAt > existing.modifiedAt -> {
                insertOrReplace(incoming)
                true
            }

            else -> false
        }
    }

    /**
     * Applies [upsertIfNewer] to all rows and returns the number of rows that
     * were inserted or updated.
     */
    @Transaction
    suspend fun upsertIfNewerAll(incoming: List<AkImportedLogEntity>): Int {
        var changedCount = 0
        incoming.forEach { entity ->
            if (upsertIfNewer(entity)) {
                changedCount++
            }
        }
        return changedCount
    }

    @Query("""
    SELECT COUNT(*) FROM ak_imported_logs
    WHERE logDateIso = :logDateIso
      AND (
        mealSlot IS NULL OR
        TRIM(mealSlot) = '' OR
        UPPER(TRIM(mealSlot)) = 'CUSTOM'
      )
""")
    fun observeUnresolvedCountForDate(logDateIso: String): Flow<Int>
}