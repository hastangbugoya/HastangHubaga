package com.example.hastanghubaga.factory

import com.example.hastanghubaga.data.local.entity.meal.MealType
import com.example.hastanghubaga.data.local.entity.supplement.SupplementDoseUnit
import com.example.hastanghubaga.domain.model.meal.Meal
import com.example.hastanghubaga.domain.model.supplement.UserSupplementSettings
import java.time.LocalDateTime
import java.time.LocalTime

object FakeUserSettingsFactory {
    fun create(
        name: String,
        scheduledTimes: List<LocalTime>
    ): UserSupplementSettings {
        return UserSupplementSettings(
            preferredServingSize = 2.0,
            preferredUnit = SupplementDoseUnit.GRAM,
            preferredServingsPerDay = 1.0,
            isEnabled = true,
            notes = "Test supplement settings",
        )
    }
}