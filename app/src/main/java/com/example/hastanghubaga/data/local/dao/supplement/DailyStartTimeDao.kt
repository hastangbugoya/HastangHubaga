package com.example.hastanghubaga.data.local.dao.supplement

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.example.hastanghubaga.data.local.entity.supplement.DailyStartTimeEntity

@Dao
interface DailyStartTimeDao {

    @Upsert
    suspend fun upsert(startTime: DailyStartTimeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setStartTime(entry: DailyStartTimeEntity)

    @Query("SELECT * FROM daily_start_time WHERE date = :date LIMIT 1")
    suspend fun getStartTime(date: String): DailyStartTimeEntity?

    @Query("DELETE FROM daily_start_time WHERE date < :threshold")
    suspend fun cleanup(threshold: String)

    /**
     * Inserts or replaces multiple DailyStartTimeEntity rows.
     * Useful for restoring from backup.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<DailyStartTimeEntity>)

    /**
     * Returns all stored DailyStartTimeEntity rows.
     * Used by the backup exporter.
     */
    @Query("SELECT * FROM daily_start_time")
    suspend fun getAll(): List<DailyStartTimeEntity>
}