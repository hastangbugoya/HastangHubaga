package com.example.hastanghubaga.domain.usecase.supplement

import com.example.hastanghubaga.data.local.entity.supplement.SupplementOccurrenceEntity
import com.example.hastanghubaga.domain.repository.supplement.SupplementOccurrenceRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

/**
 * Read-side use case for PLANNED supplement occurrences on a specific date.
 *
 * This is the planned counterpart to actual supplement dose log reads.
 *
 * Architectural role:
 * - reads from supplement_occurrences
 * - represents what the app planned for the selected date
 * - does not apply schedule math itself
 *
 * The schedule-resolution/materialization pipeline is responsible for ensuring
 * the planned rows for [date] already exist and are up to date.
 */
class GetSupplementOccurrencesForDateUseCase @Inject constructor(
    private val repository: SupplementOccurrenceRepository
) {

    operator fun invoke(
        date: LocalDate
    ): Flow<List<SupplementOccurrenceEntity>> =
        repository.observeOccurrencesForDate(date)
}