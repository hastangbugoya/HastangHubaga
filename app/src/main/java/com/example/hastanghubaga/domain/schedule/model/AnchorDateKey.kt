package com.example.hastanghubaga.domain.schedule.model

import kotlinx.datetime.LocalDate

data class AnchorDateKey(
    val anchor: TimeAnchor,
    val date: LocalDate
)
