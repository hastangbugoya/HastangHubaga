package com.example.hastanghubaga.model.entity.supplement

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "supplement_daily_log")
data class SupplementDailyLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val supplementId: Long,                // Link to SupplementEntity

    val date: Long,                        // Epoch day or timestamp
    val actualServingTaken: Double,        // e.g., 0.5, 1.0, 1.25
    val doseUnit: SupplementDoseUnit,      // Store for historical accuracy

    val timestamp: Long                    // When user recorded it
)
