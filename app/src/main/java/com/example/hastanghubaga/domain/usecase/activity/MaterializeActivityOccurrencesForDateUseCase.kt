package com.example.hastanghubaga.domain.usecase.activity

import com.example.hastanghubaga.domain.repository.activity.ActivityOccurrenceRepository
import javax.inject.Inject
import kotlinx.datetime.LocalDate

/**
 * Rebuilds the PLANNED activity occurrence snapshot for a single date.
 *
 * Canonical strategy:
 * - delegate deterministic occurrence planning to
 *   [BuildPlannedActivityOccurrencesForDateUseCase]
 * - replace that date's persisted occurrence snapshot in
 *   [ActivityOccurrenceRepository]
 *
 * Architectural intent:
 * - activity_occurrences is the canonical PLANNED ledger
 * - activity_log remains the ACTUAL ledger (future)
 * - logs may link back to planned rows by occurrenceId
 *
 * Important:
 * - one planned occurrence becomes one persisted planned row
 * - the same activity may therefore appear multiple times on the same date
 * - occurrence IDs must be stable for deterministic planned↔actual reconciliation
 */
class MaterializeActivityOccurrencesForDateUseCase @Inject constructor(
    private val buildPlannedActivityOccurrencesForDateUseCase: BuildPlannedActivityOccurrencesForDateUseCase,
    private val occurrenceRepository: ActivityOccurrenceRepository
) {

    suspend operator fun invoke(
        date: LocalDate
    ) {
        val occurrences = buildPlannedActivityOccurrencesForDateUseCase(
            date = date
        )

        occurrenceRepository.replaceOccurrencesForDate(
            date = date,
            occurrences = occurrences
        )
    }
}