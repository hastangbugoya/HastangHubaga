package com.example.hastanghubaga.domain.schedule.model

data class AnchoredTimeSpec(
    val anchor: TimeAnchor,
    val offsetMinutes: Int = 0,
    val label: String? = null,
    val sortOrderHint: Int? = null
)