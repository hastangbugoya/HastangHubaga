package com.example.hastanghubaga.model.entity.supplement

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "daily_start_time")
data class DailyStartTimeEntity(
    @PrimaryKey val date: LocalDate,
    val startTimestamp: Long // epoch millis
)
