package com.example.hastanghubaga.data.local.entity.schedule

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "schedule_rules",
    indices = [
        Index(value = ["ownerType", "ownerId"], unique = true)
    ]
)
data class ScheduleRuleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val ownerType: ScheduleOwnerType,
    val ownerId: Long,
    val recurrenceType: ScheduleRecurrenceType,
    val intervalValue: Int,
    val startDate: String,
    val endDateInclusive: String? = null,
    val isEnabled: Boolean = true,
    val timingType: ScheduleTimingType
)