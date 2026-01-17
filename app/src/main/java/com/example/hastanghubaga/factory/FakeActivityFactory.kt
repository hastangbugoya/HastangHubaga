package com.example.hastanghubaga.factory

import com.example.hastanghubaga.domain.model.activity.Activity
import com.example.hastanghubaga.domain.model.activity.ActivityType
import kotlinx.datetime.LocalDateTime

object FakeActivityFactory {
    fun create(
        name: String,
        at: LocalDateTime
    ): Activity {
        val TEST_DATE_TIME = LocalDateTime.parse("2026-01-15T12:00:00")
        val activity = Activity(
            id = 1,
            type = ActivityType.STRENGTH_TRAINING,
            start = TEST_DATE_TIME,
            end = null,
            notes = null
        )
        return activity
    }
}
