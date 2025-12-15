package com.example.hastanghubaga.data.local.entity.supplement

import androidx.room.Entity
import java.time.DayOfWeek

/**
 * Optional per-day override for an anchor time.
 *
 * Resolution priority:
 * 1) Explicit date override
 * 2) Day-of-week override (this table)
 * 3) Global default
 */
@Entity(
    tableName = "event_day_of_week_times",
    primaryKeys = ["anchor", "dayOfWeek"]
)
data class EventDayOfWeekTimeEntity(
    val anchor: DoseAnchorType,
    val dayOfWeek: DayOfWeek,
    val timeSeconds: Int
)
