package com.example.hastanghubaga.domain.time

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime


sealed interface TimeUseIntent {
    /**
     * Use the actual current instant (resolved via Clock / TimePolicy).
     * This is used when logging a dose "now".
     */
    data object ActualNow : TimeUseIntent

    /**
     * Use a scheduled time on the *current logical day*.
     * The date is resolved by the caller (e.g. Today timeline).
     */
    data class Scheduled(
        val time: LocalTime
    ) : TimeUseIntent

    /**
     * Explicit date + time chosen by the user (e.g. manual backfill).
     */
    data class Explicit(
        val date: LocalDate,
        val time: LocalTime
    ) : TimeUseIntent
}
