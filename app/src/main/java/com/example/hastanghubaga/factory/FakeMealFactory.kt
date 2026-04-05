package com.example.hastanghubaga.factory

import com.example.hastanghubaga.data.local.entity.meal.MealType
import com.example.hastanghubaga.domain.model.meal.Meal
import kotlinx.datetime.LocalDateTime

object FakeMealFactory {
    fun create(
        name: String,
        at: LocalDateTime
    ): Meal {
        return Meal(
            id = 1,
            name = name,
            type = MealType.BREAKFAST,
            treatAsAnchor = null,
            isActive = true,
            nutrition = null,
            notes = null
        )
    }
}