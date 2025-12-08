package com.example.hastanghubaga.data.local.dao.supplement

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.hastanghubaga.data.local.entity.supplement.DoseAnchorType
import com.example.hastanghubaga.data.local.entity.supplement.EventDailyOverrideEntity
import com.example.hastanghubaga.data.local.entity.supplement.EventDefaultTimeEntity

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
}
