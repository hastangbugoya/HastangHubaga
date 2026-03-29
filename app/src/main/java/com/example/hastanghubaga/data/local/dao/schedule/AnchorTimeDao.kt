package com.example.hastanghubaga.data.local.dao.schedule

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.hastanghubaga.data.local.entity.schedule.AnchorDateOverrideTimeEntity
import com.example.hastanghubaga.data.local.entity.schedule.AnchorDayOfWeekTimeEntity
import com.example.hastanghubaga.data.local.entity.schedule.AnchorDefaultTimeEntity
import com.example.hastanghubaga.data.local.entity.schedule.AnchorTimeBundle

@Dao
interface AnchorTimeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDefaultTimes(times: List<AnchorDefaultTimeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDayOfWeekOverrides(times: List<AnchorDayOfWeekTimeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDateOverrides(times: List<AnchorDateOverrideTimeEntity>)

    @Query(
        """
        SELECT * 
        FROM anchor_default_times
        """
    )
    suspend fun getDefaultTimes(): List<AnchorDefaultTimeEntity>

    @Query(
        """
        SELECT * 
        FROM anchor_day_of_week_times
        """
    )
    suspend fun getDayOfWeekOverrides(): List<AnchorDayOfWeekTimeEntity>

    @Query(
        """
        SELECT * 
        FROM anchor_date_override_times
        WHERE date = :date
        """
    )
    suspend fun getDateOverridesForDate(
        date: String
    ): List<AnchorDateOverrideTimeEntity>

    @Query(
        """
        DELETE FROM anchor_default_times
        """
    )
    suspend fun deleteAllDefaultTimes()

    @Query(
        """
        DELETE FROM anchor_day_of_week_times
        """
    )
    suspend fun deleteAllDayOfWeekOverrides()

    @Query(
        """
        DELETE FROM anchor_date_override_times
        """
    )
    suspend fun deleteAllDateOverrides()

    @Query(
        """
        DELETE FROM anchor_date_override_times
        WHERE date = :date
        """
    )
    suspend fun deleteDateOverridesForDate(
        date: String
    )

    @Transaction
    suspend fun getAnchorTimeBundleForDate(
        date: String
    ): AnchorTimeBundle {
        return AnchorTimeBundle(
            defaultTimes = getDefaultTimes(),
            dayOfWeekOverrides = getDayOfWeekOverrides(),
            dateOverrides = getDateOverridesForDate(date)
        )
    }
}