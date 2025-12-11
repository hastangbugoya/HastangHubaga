package com.example.hastanghubaga.data.local.dao.user

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import com.example.hastanghubaga.data.local.entity.user.SupplementUserSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SupplementUserSettingsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(settings: SupplementUserSettingsEntity)

    @Query("SELECT * FROM supplement_user_settings WHERE supplementId = :id LIMIT 1")
    suspend fun getSettings(id: Long): SupplementUserSettingsEntity?

    @Query("SELECT * FROM supplement_user_settings WHERE supplementId = :id LIMIT 1")
    fun observeSettings(id: Long): Flow<SupplementUserSettingsEntity?>

    @Query("DELETE FROM supplement_user_settings WHERE supplementId = :id")
    suspend fun deleteSettings(id: Long)

    @Query("SELECT * FROM supplement_user_settings")
    suspend fun getAll(): List<SupplementUserSettingsEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(entries: List<SupplementUserSettingsEntity>)
}
