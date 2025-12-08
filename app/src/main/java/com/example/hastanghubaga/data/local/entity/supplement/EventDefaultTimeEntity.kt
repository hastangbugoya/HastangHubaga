package com.example.hastanghubaga.data.local.entity.supplement

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "event_default_times")
data class EventDefaultTimeEntity(
    @PrimaryKey
    val anchor: DoseAnchorType,
    val timeSeconds: Int // LocalTime.toSecondOfDay()
)

