package com.example.hastanghubaga.testing

import com.example.hastanghubaga.widget.snapshot.BuildWidgetDailySnapshotUseCase
import com.example.hastanghubaga.domain.model.timeline.TimelineItem

/**
 * Test fake for [BuildWidgetDailySnapshotUseCase].
 *
 * This fake intentionally performs no work. It exists only to
 * satisfy constructor dependencies when testing timeline logic.
 */
class FakeBuildWidgetDailySnapshotUseCase : BuildWidgetDailySnapshotUseCase {

     suspend operator fun invoke(
        timelineItems: List<TimelineItem>
    ) {
        // no-op
    }
}
