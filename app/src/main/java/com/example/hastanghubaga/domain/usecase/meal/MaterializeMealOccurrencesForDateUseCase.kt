package com.example.hastanghubaga.domain.usecase.meal

import com.example.hastanghubaga.domain.repository.meal.MealOccurrenceRepository
import javax.inject.Inject
import kotlinx.datetime.LocalDate

/**
 * Rebuilds the PLANNED meal occurrence snapshot for a single date.
 *
 * Canonical strategy:
 * - delegate deterministic occurrence planning to
 *   [BuildPlannedMealOccurrencesForDateUseCase]
 * - replace that date's persisted occurrence snapshot in
 *   [MealOccurrenceRepository]
 *
 * Architectural intent:
 * - meal_occurrences is the canonical PLANNED ledger
 * - meal_log remains the ACTUAL ledger
 * - logs may link back to planned rows by occurrenceId
 *
 * Important:
 * - one planned occurrence becomes one persisted planned row
 * - the same meal may therefore appear multiple times on the same date
 * - occurrence IDs must be stable for deterministic planned↔actual reconciliation
 */
class MaterializeMealOccurrencesForDateUseCase @Inject constructor(
    private val buildPlannedMealOccurrencesForDateUseCase: BuildPlannedMealOccurrencesForDateUseCase,
    private val occurrenceRepository: MealOccurrenceRepository
) {

    suspend operator fun invoke(
        date: LocalDate
    ) {
        val occurrences = buildPlannedMealOccurrencesForDateUseCase(
            date = date
        )

        occurrenceRepository.replaceOccurrencesForDate(
            date = date,
            occurrences = occurrences
        )
    }
}