package com.example.hastanghubaga.factory

import com.example.hastanghubaga.domain.model.activity.Activity
import com.example.hastanghubaga.domain.model.activity.ActivityType
import kotlinx.datetime.LocalDateTime

object FakeActivityFactory {
    fun create(
        name: String,
        at: LocalDateTime
    ): Activity {
        val activity = Activity(
            id = 1,
            title = name,
            type = ActivityType.STRENGTH_TRAINING,
            start = at,
            end = null,
            notes = null
        )
        return activity
    }
}