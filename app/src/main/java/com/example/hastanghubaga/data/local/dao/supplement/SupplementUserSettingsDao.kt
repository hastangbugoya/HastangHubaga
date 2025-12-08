package com.example.hastanghubaga.data.local.dao.supplement

import androidx.room.*
import com.example.hastanghubaga.data.local.entity.user.SupplementUserSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SupplementUserSettingsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(settings: SupplementUserSettingsEntity)

    @Query("SELECT * FROM supplement_user_settings WHERE supplementId = :supplementId LIMIT 1")
    suspend fun getSettings(supplementId: Long): SupplementUserSettingsEntity?

    @Query("SELECT * FROM supplement_user_settings WHERE supplementId = :supplementId LIMIT 1")
    fun observeSettings(supplementId: Long): Flow<SupplementUserSettingsEntity?>

    @Query("DELETE FROM supplement_user_settings WHERE supplementId = :supplementId")
    suspend fun deleteSettings(supplementId: Long)
}