package com.example.hastanghubaga.data.local.entity.supplement

import androidx.room.Entity

@Entity(
    tableName = "event_daily_overrides",
    primaryKeys = ["date", "anchor"]
)
data class EventDailyOverrideEntity(
    val date: String,               // YYYY-MM-DD
    val anchor: DoseAnchorType,
    val timeSeconds: Int            // override time for this day
)
