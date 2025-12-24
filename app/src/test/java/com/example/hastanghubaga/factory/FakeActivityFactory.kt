package com.example.hastanghubaga.factory

import com.example.hastanghubaga.data.local.entity.meal.MealType
import com.example.hastanghubaga.domain.model.activity.Activity
import com.example.hastanghubaga.domain.model.activity.ActivityType
import com.example.hastanghubaga.domain.model.meal.Meal
import kotlinx.datetime.LocalDateTime

object FakeActivityFactory {

    private var nextId = 1L
    fun create(
        name: String,
        at: LocalDateTime,
        type: ActivityType = ActivityType.STRENGTH_TRAINING,
        notes: String? = null
    ): Activity =
        Activity(
            id = nextId++,
            start = at,          // ✅ critical mapping
            end = null,
            type = type,
            notes = notes
        )
}