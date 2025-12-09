package com.example.hastanghubaga.data.local.dao.user

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.hastanghubaga.data.local.entity.user.SupplementUserSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SupplementUserSettingsDao {

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun upsert(settings: SupplementUserSettingsEntity)

    @Query("SELECT * FROM supplement_user_settings WHERE supplementId = :supplementId LIMIT 1")
    suspend fun getSettings(supplementId: Long): SupplementUserSettingsEntity?

    @Query("SELECT * FROM supplement_user_settings WHERE supplementId = :supplementId LIMIT 1")
    fun observeSettings(supplementId: Long): Flow<SupplementUserSettingsEntity?>

    @Query("DELETE FROM supplement_user_settings WHERE supplementId = :supplementId")
    suspend fun deleteSettings(supplementId: Long)

    @Query("SELECT * FROM supplement_user_settings")
    suspend fun getAll(): List<SupplementUserSettingsEntity>

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    fun insertAll(entries: List<SupplementUserSettingsEntity>)
}