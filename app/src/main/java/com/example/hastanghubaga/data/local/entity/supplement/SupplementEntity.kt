package com.example.hastanghubaga.data.local.entity.supplement

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.DayOfWeek

@Entity(tableName = "supplements")
data class SupplementEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val brand: String?,
    val notes: String? = null,

    val recommendedServingSize: Double,    // e.g., 2 capsules
    val recommendedDoseUnit: SupplementDoseUnit,
    val servingsPerDay: Int,
    val recommendedWithFood: Boolean? = null,   // recommended to be taken with food
    val recommendedLiquidInOz: Double? = null,  // recommended with liquid in oz
    val recommendedTimeBetweenDailyDosesMinutes: Int? = null,
    val avoidCaffeine: Boolean? = null, // should avoid taking with caffeine

    val frequencyType: FrequencyType = FrequencyType.DAILY,
    val frequencyInterval: Int? = null,              // used when EVERY_X_DAYS
    val weeklyDays: List<DayOfWeek>? = null,         // used when WEEKLY
    val offsetMinutes: Int? = null,

    val customDose: Double? = null,
    val customDoseUnit: SupplementDoseUnit? = null,
    val startDate: String? = null, // ISO-8601 "YYYY-MM-DD"
    val lastTakenDate: String? = null, // ISO-8601 "YYYY-MM-DD"

    val isActive: Boolean = true
)