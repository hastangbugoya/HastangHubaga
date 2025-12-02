package com.example.hastanghubaga.model.local.db

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.hastanghubaga.model.entity.supplement.SupplementEntity
import kotlinx.coroutines.flow.Flow

interface SupplementEntityDao {
    // Insert or update a supplement
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplement(supplement: SupplementEntity): Long

    // Insert multiple supplements
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplements(list: List<SupplementEntity>)

    // Delete a supplement
    @Delete
    suspend fun deleteSupplement(supplement: SupplementEntity)

    // Update
    @Update
    suspend fun updateSupplement(supplement: SupplementEntity)

    // Get single supplement by ID
    @Query("SELECT * FROM supplements WHERE id = :id LIMIT 1")
    suspend fun getSupplementById(id: Long): SupplementEntity?

    // Reactive list for UI
    @Query("SELECT * FROM supplements ORDER BY name ASC")
    fun getAllSupplementsFlow(): Flow<List<SupplementEntity>>

    // Non-reactive list (e.g., for workers)
    @Query("SELECT * FROM supplements ORDER BY name ASC")
    suspend fun getAllSupplements(): List<SupplementEntity>

    // Search (for UI autocomplete)
    @Query("SELECT * FROM supplements WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchSupplements(query: String): Flow<List<SupplementEntity>>
}