package com.example.hastanghubaga.domain.usecase.supplement

import com.example.hastanghubaga.data.local.entity.meal.AkImportedMealEntity
import com.example.hastanghubaga.domain.model.meal.Meal
import com.example.hastanghubaga.domain.repository.supplement.SupplementOccurrenceRepository
import javax.inject.Inject
import kotlinx.datetime.LocalDate

/**
 * Rebuilds the PLANNED supplement occurrence snapshot for a single date.
 *
 * Canonical strategy:
 * - delegate deterministic occurrence planning to
 *   [BuildPlannedSupplementOccurrencesForDateUseCase]
 * - replace that date's persisted occurrence snapshot in
 *   [SupplementOccurrenceRepository]
 *
 * Architectural intent:
 * - supplement_occurrences is the canonical PLANNED ledger
 * - supplement_daily_log remains the ACTUAL ledger
 * - logs may link back to planned rows by occurrenceId
 *
 * Important:
 * - one planned occurrence becomes one persisted planned row
 * - the same supplement may therefore appear multiple times on the same date
 * - occurrence IDs must be stable for deterministic planned↔actual reconciliation
 */
class MaterializeSupplementOccurrencesForDateUseCase @Inject constructor(
    private val buildPlannedSupplementOccurrencesForDateUseCase: BuildPlannedSupplementOccurrencesForDateUseCase,
    private val occurrenceRepository: SupplementOccurrenceRepository
) {

    suspend operator fun invoke(
        date: LocalDate,
        meals: List<Meal> = emptyList(),
        importedMeals: List<AkImportedMealEntity> = emptyList()
    ) {
        val occurrences = buildPlannedSupplementOccurrencesForDateUseCase(
            date = date,
            meals = meals,
            importedMeals = importedMeals
        )

        occurrenceRepository.replaceOccurrencesForDate(
            date = date,
            occurrences = occurrences
        )
    }
}