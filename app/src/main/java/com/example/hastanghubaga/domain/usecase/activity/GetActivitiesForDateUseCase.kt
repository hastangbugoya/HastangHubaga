package com.example.hastanghubaga.domain.usecase.activity

import com.example.hastanghubaga.domain.model.activity.Activity
import com.example.hastanghubaga.domain.repository.activity.ActivityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.time.LocalDate
import javax.inject.Inject

/**
 * Returns activities for a given date.
 *
 * CURRENT BEHAVIOR:
 * - Always emits an empty list
 *
 * FUTURE:
 * - Will observe activity logs from repository
 * - Will emit reactive updates when activities are added/edited
 */
class GetActivitiesForDateUseCase @Inject constructor(
    private val activityRepository: ActivityRepository
) {

    operator fun invoke(date: LocalDate): Flow<List<Activity>> {
        return flowOf(emptyList())
    }
}
