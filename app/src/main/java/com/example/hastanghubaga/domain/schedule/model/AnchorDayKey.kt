package com.example.hastanghubaga.domain.schedule.model

import kotlinx.datetime.DayOfWeek

data class AnchorDayKey(
    val anchor: TimeAnchor,
    val dayOfWeek: DayOfWeek
)
