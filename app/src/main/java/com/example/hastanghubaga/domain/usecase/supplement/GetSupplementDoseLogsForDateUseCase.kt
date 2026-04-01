package com.example.hastanghubaga.domain.usecase.supplement

import com.example.hastanghubaga.domain.model.supplement.SupplementDoseLog
import com.example.hastanghubaga.domain.repository.supplement.SupplementDoseLogReadRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

/**
 * Returns all actual supplement dose logs for a given date.
 *
 * This use case:
 * - Observes supplement dose logs reactively
 * - Applies date scoping only
 * - Does NOT perform schedule reconciliation
 * - Does NOT decide whether a log was planned, extra, or make-up
 * - Does NOT perform sorting or timeline mapping beyond repository ordering
 *
 * Important:
 * A dose log represents what the user says happened.
 * It may or may not correspond to a planned schedule rule.
 *
 * This keeps the read path aligned with HH's principle that the app may
 * remind and suggest, but the user remains authoritative about what happened.
 */
class GetSupplementDoseLogsForDateUseCase @Inject constructor(
    private val repository: SupplementDoseLogReadRepository
) {

    operator fun invoke(date: LocalDate): Flow<List<SupplementDoseLog>> {
        return repository.observeDoseLogsForDate(date)
    }
}