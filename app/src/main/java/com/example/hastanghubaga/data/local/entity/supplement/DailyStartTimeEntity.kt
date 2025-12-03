package com.example.hastanghubaga.data.local.entity.supplement

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "daily_start_time")
data class DailyStartTimeEntity(
    @PrimaryKey val date: String,
    val hourZero: Int    // e.g. 3600 for 1 AM
)
