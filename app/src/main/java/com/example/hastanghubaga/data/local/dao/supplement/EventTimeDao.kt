package com.example.hastanghubaga.data.local.dao.supplement

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.example.hastanghubaga.data.local.entity.supplement.DoseAnchorType
import com.example.hastanghubaga.data.local.entity.supplement.EventDailyOverrideEntity
import com.example.hastanghubaga.data.local.entity.supplement.EventDayOfWeekTimeEntity
import com.example.hastanghubaga.data.local.entity.supplement.EventDefaultTimeEntity
import java.time.DayOfWeek

@Dao
interface EventTimeDao {

    // --- DEFAULT EVENT TIMES ---
    @Query("SELECT * FROM event_default_times WHERE anchor = :anchor")
    suspend fun getDefault(anchor: DoseAnchorType): EventDefaultTimeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDefault(time: EventDefaultTimeEntity)

    @Query("SELECT * FROM event_default_times")
    suspend fun getAllDefaults(): List<EventDefaultTimeEntity>

    // --- DAILY OVERRIDES ---
    @Query("""
        SELECT * FROM event_daily_overrides 
        WHERE date = :date AND anchor = :anchor
    """)
    suspend fun getOverride(date: String, anchor: DoseAnchorType): EventDailyOverrideEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertOverride(override: EventDailyOverrideEntity)

    @Query("DELETE FROM event_daily_overrides WHERE date = :date AND anchor = :anchor")
    suspend fun removeOverride(date: String, anchor: DoseAnchorType)

    // Backup support
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllDefault(entries: List<EventDefaultTimeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllOverrides(entries: List<EventDailyOverrideEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(entries: List<EventDailyOverrideEntity>)

    @Query("SELECT * FROM event_daily_overrides")
    suspend fun getAllOverrides(): List<EventDailyOverrideEntity>

    @Query("""
    SELECT * FROM event_day_of_week_times
    WHERE anchor = :anchor AND dayOfWeek = :day
""")
    suspend fun getDayOfWeekOverride(
        anchor: DoseAnchorType,
        day: DayOfWeek
    ): EventDayOfWeekTimeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDayOfWeekOverride(
        entity: EventDayOfWeekTimeEntity
    )

    @Query("SELECT * FROM event_day_of_week_times")
    suspend fun getAllDayOfWeekOverrides(): List<EventDayOfWeekTimeEntity>

    @Upsert
    fun upsertDailyOverride(eventDailyOverrideEntity: EventDailyOverrideEntity)

    @Query("SELECT * FROM event_default_times")
    suspend fun debugGetAll(): List<EventDefaultTimeEntity>


}
