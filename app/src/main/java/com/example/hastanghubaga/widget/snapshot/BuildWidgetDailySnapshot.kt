package com.example.hastanghubaga.widget.snapshot

import kotlinx.datetime.LocalDate

/**
 * Contract for building (and typically persisting) a widget snapshot for a single day.
 *
 * This interface exists so callers (Today timeline, workers, alarms) can trigger snapshot
 * creation without knowing the details of repositories, persistence, or widget rendering.
 */
fun interface BuildWidgetDailySnapshot {
    suspend operator fun invoke(day: LocalDate)
}
