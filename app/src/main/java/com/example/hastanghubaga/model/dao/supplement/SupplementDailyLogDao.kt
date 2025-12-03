package com.example.hastanghubaga.model.dao.supplement

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.hastanghubaga.model.entity.supplement.SupplementDailyLogEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface SupplementDailyLogDao {
    // Insert or update a logged dose
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDoseLog(log: SupplementDailyLogEntity): Long

    // Insert multiple entries
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDoseLogs(logs: List<SupplementDailyLogEntity>)

    // Delete a log entry
    @Delete
    suspend fun deleteDoseLog(log: SupplementDailyLogEntity)

    // Get all logs for a specific day
    @Query("SELECT * FROM supplement_daily_log WHERE date = :date ORDER BY timestamp ASC")
    fun getDoseLogsForDay(date: String): Flow<List<SupplementDailyLogEntity>>

    // Non-reactive version
    @Query("SELECT * FROM supplement_daily_log WHERE date = :date ORDER BY timestamp ASC")
    suspend fun getDoseLogsForDayOnce(date: String): List<SupplementDailyLogEntity>

    // Logs for a specific supplement
    @Query("SELECT * FROM supplement_daily_log WHERE supplementId = :supplementId ORDER BY date DESC, timestamp ASC")
    fun getLogsForSupplement(supplementId: Long): Flow<List<SupplementDailyLogEntity>>

    // Get a single log by id
    @Query("SELECT * FROM supplement_daily_log WHERE id = :id LIMIT 1")
    suspend fun getDoseLogById(id: Long): SupplementDailyLogEntity?

    // Delete all logs older than X days
    @Query("DELETE FROM supplement_daily_log WHERE date < :threshold")
    suspend fun deleteLogsBefore(threshold: String)
}