package com.example.hastanghubaga.data.local.entity.schedule

data class AnchorTimeBundle(
    val defaultTimes: List<AnchorDefaultTimeEntity>,
    val dayOfWeekOverrides: List<AnchorDayOfWeekTimeEntity>,
    val dateOverrides: List<AnchorDateOverrideTimeEntity>
)
