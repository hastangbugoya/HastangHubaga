package com.example.hastanghubaga.factory

import com.example.hastanghubaga.data.local.entity.meal.MealType
import com.example.hastanghubaga.domain.model.activity.Activity
import com.example.hastanghubaga.domain.model.activity.ActivityType
import com.example.hastanghubaga.domain.model.meal.Meal
import java.time.LocalDateTime
import java.time.LocalTime

object FakeActivityFactory {

    fun create(
        name: String,
        at: LocalDateTime
    ): Activity {
        return Activity(
            id = 1L,
            type = ActivityType.STRENGTH_TRAINING,
            start = at,
            end = at.plusMinutes(60),
            notes = name
        )
    }
}