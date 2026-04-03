package com.example.hastanghubaga.domain.usecase.activity

import com.example.hastanghubaga.data.local.entity.activity.ActivityOccurrenceEntity
import com.example.hastanghubaga.domain.repository.activity.ActivityOccurrenceRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

/**
 * Observes the planned activity occurrence snapshot for a single date.
 *
 * Canonical role:
 * - ActivityOccurrenceEntity is the PLANNED day-level ledger for activities
 * - timeline/planner consumers should read planned activity presence from here
 * - actual/manual activity rows remain a separate concern
 *
 * This mirrors the supplement planned-occurrence read path.
 */
class GetActivityOccurrencesForDateUseCase @Inject constructor(
    private val activityOccurrenceRepository: ActivityOccurrenceRepository
) {
    operator fun invoke(
        date: LocalDate
    ): Flow<List<ActivityOccurrenceEntity>> {
        return activityOccurrenceRepository.observeOccurrencesForDate(date)
    }
}