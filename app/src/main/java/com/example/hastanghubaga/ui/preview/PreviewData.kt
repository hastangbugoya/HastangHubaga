package com.example.hastanghubaga.ui.preview

import com.example.hastanghubaga.domain.model.supplement.Supplement
import com.example.hastanghubaga.domain.model.supplement.SupplementWithUserSettings
import com.example.hastanghubaga.domain.model.supplement.UserSupplementSettings
import com.example.hastanghubaga.data.local.entity.supplement.DoseAnchorType
import com.example.hastanghubaga.data.local.entity.supplement.FrequencyType
import com.example.hastanghubaga.data.local.entity.supplement.SupplementDoseUnit
import com.example.hastanghubaga.domain.model.supplement.MealAwareDoseState
import java.time.LocalTime

/**
 * Centralized fake data for Compose previews.
 *
 * IMPORTANT:
 * - This file is UI-only
 * - Never used in production logic
 * - Populate ONLY fields used by UI components
 */
object PreviewData {
    val supplementWithCaffeineWarning = SupplementWithUserSettings(
        supplement = Supplement(
            id = 1L,
            name = "Vitamin C",
            recommendedServingSize = 2.0,
            recommendedDoseUnit = SupplementDoseUnit.MG,
            avoidCaffeine = true,
            doseAnchorType = DoseAnchorType.BREAKFAST,
            servingsPerDay = 1,
            frequencyType = FrequencyType.DAILY,
            recommendedWithFood = null,
            recommendedLiquidInOz = null,
            recommendedTimeBetweenDailyDosesMinutes = null,
            doseConditions = emptySet(),
            frequencyInterval = null,
            weeklyDays = emptyList(),
            offsetMinutes = null,
            startDate = null,
            lastTakenDate = null,
            ingredients = emptyList(),
            isActive = true,
            brand = "Brand X",
            notes = "notes",
        ),
        userSettings = null,
        doseState = MealAwareDoseState.Ready,
        scheduledTimes = emptyList()
    )

    val supplementWithoutCaffeineWarning = SupplementWithUserSettings(
        supplement = supplementWithCaffeineWarning.supplement.copy(
            avoidCaffeine = false,
            name = "Magnesium"
        ),
        userSettings = null,
        doseState = MealAwareDoseState.Ready,
        scheduledTimes = emptyList()
    )

    val supplementWithUserOverride = SupplementWithUserSettings(
        supplement = supplementWithCaffeineWarning.supplement,
        userSettings = UserSupplementSettings(
            preferredServingSize = 1.5,
            preferredUnit = SupplementDoseUnit.MG,
            preferredServingsPerDay = null,
            isEnabled = true,
            notes = null
        ),
        doseState = MealAwareDoseState.Ready,
        scheduledTimes = emptyList()
    )

    val supplementList = listOf(
        supplementWithCaffeineWarning,
        supplementWithoutCaffeineWarning,
        supplementWithUserOverride
    )
}

