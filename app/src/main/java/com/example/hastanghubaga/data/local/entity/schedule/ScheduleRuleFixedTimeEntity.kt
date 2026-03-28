package com.example.hastanghubaga.data.local.entity.schedule

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "schedule_rule_fixed_times",
    foreignKeys = [
        ForeignKey(
            entity = ScheduleRuleEntity::class,
            parentColumns = ["id"],
            childColumns = ["scheduleRuleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["scheduleRuleId"])
    ]
)
data class ScheduleRuleFixedTimeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val scheduleRuleId: Long,
    val time: String,
    val label: String? = null,
    val sortOrderHint: Int? = null
)
