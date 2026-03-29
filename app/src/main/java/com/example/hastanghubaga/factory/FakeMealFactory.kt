package com.example.hastanghubaga.factory

import com.example.hastanghubaga.data.local.entity.meal.MealType
import com.example.hastanghubaga.domain.model.meal.Meal
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime

object FakeMealFactory {
    fun create(
        name: String,
        at: LocalDateTime
    ): Meal {
        val TEST_DATE_TIME = LocalDateTime.parse("2026-01-15T12:00:00")
        val meal = Meal(
            id = 1,
            name = "Test Breakfast",
            type = MealType.BREAKFAST,
            timestamp = TEST_DATE_TIME,
            nutrition = null,
            notes = null
        )
        return meal
    }
}
