package com.example.hastanghubaga.data.local.entity.schedule

import androidx.room.Embedded
import androidx.room.Relation

data class ScheduleRuleWithDetails(
    @Embedded
    val rule: ScheduleRuleEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "scheduleRuleId"
    )
    val weeklyDays: List<ScheduleRuleWeeklyDayEntity>,

    @Relation(
        parentColumn = "id",
        entityColumn = "scheduleRuleId"
    )
    val fixedTimes: List<ScheduleRuleFixedTimeEntity>,

    @Relation(
        parentColumn = "id",
        entityColumn = "scheduleRuleId"
    )
    val anchoredTimes: List<ScheduleRuleAnchoredTimeEntity>
)
