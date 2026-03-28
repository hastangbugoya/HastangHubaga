package com.example.hastanghubaga.data.local.entity.schedule

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "schedule_rule_weekly_days",
    primaryKeys = ["scheduleRuleId", "dayOfWeek"],
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
data class ScheduleRuleWeeklyDayEntity(
    val scheduleRuleId: Long,
    val dayOfWeek: String
)