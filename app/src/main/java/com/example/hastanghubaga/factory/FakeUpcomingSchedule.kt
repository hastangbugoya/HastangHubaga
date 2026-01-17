package com.example.hastanghubaga.factory

import com.example.hastanghubaga.domain.model.timeline.UpcomingSchedule
import com.example.hastanghubaga.testing.TestTimestamps
import com.example.hastanghubaga.ui.timeline.TodayUiRowType
import kotlinx.datetime.LocalDateTime

object FakeUpcomingSchedule {
    fun create(
        name: String,
        at: LocalDateTime
    ): UpcomingSchedule {
        val TEST_DATE_TIME = LocalDateTime.parse("2026-01-15T12:00:00")
        val upcomingschedule = UpcomingSchedule(
            id = 1,
            type = TodayUiRowType.ACTIVITY,
            referenceId = 10L,
            scheduledAt = TEST_DATE_TIME,
            title = "Sample Activity",
            subtitle = null,
            isCompleted = false
        )
        return upcomingschedule
    }
}
