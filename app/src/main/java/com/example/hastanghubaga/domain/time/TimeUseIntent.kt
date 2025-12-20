package com.example.hastanghubaga.domain.time

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime


sealed interface TimeUseIntent {
    data object ActualNow : TimeUseIntent
    data class Scheduled(val time: LocalTime) : TimeUseIntent
    data class Explicit(val date: LocalDate, val time: LocalTime) : TimeUseIntent
}
