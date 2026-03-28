package com.example.hastanghubaga.data.local.entity.schedule

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "anchor_day_of_week_times",
    primaryKeys = ["anchor", "dayOfWeek"],
    indices = [
        Index(value = ["dayOfWeek"])
    ]
)
data class AnchorDayOfWeekTimeEntity(
    val anchor: String,
    val dayOfWeek: String,
    val timeSeconds: Int
)
