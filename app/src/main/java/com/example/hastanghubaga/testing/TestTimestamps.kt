package com.example.hastanghubaga.testing

import java.time.LocalDateTime

object TestTimestamps {
    val BASE: LocalDateTime = LocalDateTime.of(2026, 1, 15, 12, 0)
    val OVERDUE: LocalDateTime = BASE.minusHours(1)
    val DUE_SOON: LocalDateTime = BASE.plusMinutes(30)
    val LATER: LocalDateTime = BASE.plusHours(3)
}
