package com.example.hastanghubaga.model.dao.supplement

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.example.hastanghubaga.model.entity.supplement.DailyStartTimeEntity
import java.time.LocalDate

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
}