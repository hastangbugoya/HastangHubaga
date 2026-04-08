package com.example.hastanghubaga.data.local.dao.location

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.hastanghubaga.data.local.entity.location.AddressEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for managing reusable saved addresses.
 *
 * Scope (DB foundation only):
 * - basic CRUD
 * - observe all / favorites
 *
 * No business rules here:
 * - no enforcement of saved vs raw usage
 * - no map/geocode assumptions
 *
 * Future AI/dev reminders:
 * - do NOT auto-dedupe addresses silently
 * - allow duplicate human-readable addresses (user intent matters)
 * - sorting/weighting (favorites, recents) should be done at query or use-case layer
 */
@Dao
interface AddressDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(address: AddressEntity): Long

    @Update
    suspend fun update(address: AddressEntity)

    @Delete
    suspend fun delete(address: AddressEntity)

    @Query("SELECT * FROM addresses ORDER BY isFavorite DESC, updatedAt DESC")
    fun observeAll(): Flow<List<AddressEntity>>

    @Query("SELECT * FROM addresses WHERE isFavorite = 1 ORDER BY updatedAt DESC")
    fun observeFavorites(): Flow<List<AddressEntity>>

    @Query("SELECT * FROM addresses WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): AddressEntity?
}