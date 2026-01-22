package com.example.hastanghubaga.domain.usecase.nutrition

import com.example.hastanghubaga.domain.model.nutrition.UserNutritionGoals
import com.example.hastanghubaga.domain.model.nutrition.toDomain
import com.example.hastanghubaga.domain.repository.nutrition.NutritionGoalsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Observes the currently active user nutrition goal and maps it into
 * a domain model for business logic and UI consumption.
 *
 * Returns `null` when no active goal exists.
 */
class ObserveActiveUserNutritionGoalUseCase @Inject constructor(
    private val repository: NutritionGoalsRepository
) {

    operator fun invoke(): Flow<UserNutritionGoals?> {
        return repository.observeActiveGoal()
            .map { entity ->
                entity?.toDomain()
            }
    }
}