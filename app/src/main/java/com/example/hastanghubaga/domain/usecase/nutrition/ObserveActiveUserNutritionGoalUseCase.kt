package com.example.hastanghubaga.domain.usecase.nutrition

import com.example.hastanghubaga.domain.model.nutrition.NutritionPlan
import com.example.hastanghubaga.domain.repository.nutrition.NutritionGoalsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Observes all currently active nutrition plans.
 *
 * This replaces the old single-active-goal assumption.
 *
 * Current architecture rules:
 * - HH may have multiple active nutrition plans at the same time
 * - effective nutrient resolution happens later in a separate layer
 * - this use case only exposes the active parent plans
 *
 * Future cleanup:
 * - rename this file/class to ObserveActiveNutritionPlansUseCase after
 *   older references are migrated
 */
class ObserveActiveUserNutritionGoalUseCase @Inject constructor(
    private val repository: NutritionGoalsRepository
) {

    operator fun invoke(): Flow<List<NutritionPlan>> {
        return repository.observeActivePlans()
    }
}