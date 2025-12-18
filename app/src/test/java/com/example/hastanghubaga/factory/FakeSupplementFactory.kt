package com.example.hastanghubaga.factory

import com.example.hastanghubaga.data.local.entity.supplement.DoseAnchorType
import com.example.hastanghubaga.data.local.entity.supplement.FrequencyType
import com.example.hastanghubaga.data.local.entity.supplement.SupplementDoseUnit
import com.example.hastanghubaga.domain.model.supplement.Supplement
import com.example.hastanghubaga.domain.model.supplement.Ingredient
import java.time.DayOfWeek

object FakeSupplementFactory {

    fun create(
        id: Long = 1L,
        name: String,
        anchor: DoseAnchorType = DoseAnchorType.BREAKFAST
    ): Supplement {
        return Supplement(
            id = id,
            name = name,

            // Core dosing
            recommendedServingSize = 1.0,
            recommendedDoseUnit = SupplementDoseUnit.CAPSULE,
            servingsPerDay = 1.0,

            // Scheduling
            doseAnchorType = anchor,
            frequencyType = FrequencyType.DAILY,
            frequencyInterval = 1,
            weeklyDays = emptyList<DayOfWeek>(),
            offsetMinutes = 0,

            // Food / timing guidance
            recommendedWithFood = false,
            recommendedLiquidInOz = null,
            recommendedTimeBetweenDailyDosesMinutes = null,
            avoidCaffeine = false,

            // Lifecycle
            startDate = null,
            lastTakenDate = null,
            isActive = true,

            // Metadata
            brand = "Brand X",
            notes = "Test supplement",
            ingredients = emptyList<Ingredient>(),
            doseConditions = emptySet()
        )

    }
}
