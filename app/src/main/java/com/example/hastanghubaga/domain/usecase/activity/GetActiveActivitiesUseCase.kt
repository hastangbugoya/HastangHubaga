package com.example.hastanghubaga.domain.usecase.activity

import com.example.hastanghubaga.domain.model.activity.Activity
import com.example.hastanghubaga.domain.repository.activity.ActivityRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

/**
 * Returns all active activities.
 *
 * This use case is intentionally simple:
 * - it exposes active activity choices for UI flows such as force-log
 * - it does NOT apply timeline/date scheduling rules
 * - it does NOT decide whether an activity should appear on a specific day
 *
 * Important:
 * Force-log is an "actual happened" flow, not a planned-schedule flow.
 * The user may choose any active activity even if it is not currently scheduled.
 *
 * Future AI/dev reminder:
 * Meal force-log should follow this same pattern:
 * active picker source separate from day/timeline occurrence queries.
 */
class GetActiveActivitiesUseCase @Inject constructor(
    private val repository: ActivityRepository
) {

    operator fun invoke(): Flow<List<Activity>> {
        return repository.observeActiveActivities()
    }
}