package com.example.hastanghubaga.testing

import com.example.hastanghubaga.widget.snapshot.BuildWidgetDailySnapshot
import kotlinx.datetime.LocalDate

/**
 * Test fake for [BuildWidgetDailySnapshot].
 *
 * This fake intentionally performs no work.
 * It exists only to satisfy constructor dependencies when testing
 * timeline-related use cases that trigger widget snapshot generation
 * as a side effect.
 *
 * Why this exists:
 * - Production code builds and persists a widget snapshot
 * - Unit tests for timeline logic should not touch widget state
 * - This fake allows tests to run without mocking or I/O
 *
 * IMPORTANT:
 * This fake must implement the *interface* ([BuildWidgetDailySnapshot]),
 * NOT the concrete production class.
 */
class FakeBuildWidgetDailySnapshotUseCase : BuildWidgetDailySnapshot {

    override suspend operator fun invoke(day: LocalDate) {
        // no-op by design
    }
}
