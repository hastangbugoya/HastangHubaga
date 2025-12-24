package com.example.hastanghubaga.factory

import com.example.hastanghubaga.domain.model.supplement.MealAwareDoseState
import com.example.hastanghubaga.domain.model.supplement.SupplementWithUserSettings
import kotlinx.datetime.LocalTime

object FakeSupplementWithUserSettingsFactory {

    fun create(
        name: String,
        scheduledTimes: List<LocalTime>
    ): SupplementWithUserSettings {
        return SupplementWithUserSettings(
            supplement = FakeSupplementFactory.create(name = name),
            userSettings = FakeUserSettingsFactory.create(
                name,
                scheduledTimes = emptyList()
            ),
            doseState = MealAwareDoseState.Ready,
            scheduledTimes = scheduledTimes
        )
    }
}
