package com.example.hastanghubaga.factory

import com.example.hastanghubaga.data.local.entity.meal.MealType
import com.example.hastanghubaga.domain.model.meal.Meal
import com.example.hastanghubaga.domain.model.supplement.MealAwareDoseState
import com.example.hastanghubaga.domain.model.supplement.SupplementWithUserSettings
import kotlinx.datetime.LocalDateTime

object FakeMealFactory {
    fun create(
        name: String,
        at: LocalDateTime
    ): Meal {
        return Meal(
            id = 1L,
            type = MealType.BREAKFAST,
            timestamp = at,
            nutrition = null,
            notes = name,
            name = name
        )
    }
}