package com.example.hastanghubaga.factory

import com.example.hastanghubaga.data.local.entity.supplement.DoseAnchorType
import com.example.hastanghubaga.data.local.entity.supplement.FrequencyType
import com.example.hastanghubaga.data.local.entity.supplement.IngredientUnit
import com.example.hastanghubaga.domain.model.supplement.Supplement
import com.example.hastanghubaga.domain.model.supplement.SupplementWithUserSettings
import com.example.hastanghubaga.domain.model.supplement.UserSupplementSettings
import com.example.hastanghubaga.domain.model.supplement.MealAwareDoseState
import com.example.hastanghubaga.data.local.entity.supplement.SupplementDoseUnit
import com.example.hastanghubaga.domain.model.supplement.Ingredient
import kotlinx.datetime.LocalTime
import java.time.DayOfWeek

/**
 * Test factory for creating [SupplementWithUserSettings].
 *
 * Purpose:
 * - Keep unit tests short and readable
 * - Avoid repeating verbose constructors
 * - Make intent obvious (name + scheduled times)
 *
 * This factory intentionally fills in "reasonable defaults"
 * for fields that are irrelevant to timeline tests.
 */
object FakeSupplementWithUserSettingsFactory {

    fun create(
        name: String,
        scheduledTimes: List<LocalTime>
    ): SupplementWithUserSettings {

        val supplement = Supplement(
            id = 1L,
            name = name,
            recommendedServingSize = 1.0,
            recommendedDoseUnit = SupplementDoseUnit.CAPSULE,
            servingsPerDay = scheduledTimes.size.toDouble(),
            isActive = true,
            brand = "Brand X",
            notes = "Notes",
            recommendedWithFood = false,
            recommendedLiquidInOz = 1.0,
            recommendedTimeBetweenDailyDosesMinutes = 120,
            avoidCaffeine = true,
            doseConditions = setOf(),
            doseAnchorType = DoseAnchorType.BREAKFAST,
            frequencyType = FrequencyType.DAILY,
            frequencyInterval = 2,
            weeklyDays = listOf<DayOfWeek>(),
            offsetMinutes = 0,
            startDate = "2025-02-02",
            lastTakenDate = "2026-01-05",
            ingredients = listOf(Ingredient(2,"Vitamin B1 (Thiamine)", IngredientUnit.MG, 1.2, null, null, null, null)),
        )

        val userSettings: UserSupplementSettings? = null
        // or, if needed later:
        // UserSupplementSettings(
        //     supplementId = supplement.id,
        //     preferredServingSize = null,
        //     preferredUnit = null,
        //     preferredServingsPerDay = null,
        //     isEnabled = true,
        //     notes = null
        // )

        return SupplementWithUserSettings(
            supplement = supplement,
            userSettings = userSettings,
            doseState = MealAwareDoseState.Ready,
            scheduledTimes = scheduledTimes
        )
    }
}
