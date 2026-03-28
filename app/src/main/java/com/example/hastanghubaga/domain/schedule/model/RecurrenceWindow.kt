package com.example.hastanghubaga.domain.schedule.model

import kotlinx.datetime.LocalDate

data class RecurrenceWindow(
    val startDate: LocalDate,
    val endDateInclusive: LocalDate? = null
)