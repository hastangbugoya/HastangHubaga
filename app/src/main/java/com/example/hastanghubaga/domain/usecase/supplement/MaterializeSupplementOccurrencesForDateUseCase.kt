package com.example.hastanghubaga.domain.usecase.supplement

import com.example.hastanghubaga.data.local.entity.supplement.SupplementOccurrenceEntity
import com.example.hastanghubaga.data.local.entity.supplement.SupplementOccurrenceSourceType
import com.example.hastanghubaga.domain.repository.supplement.SupplementOccurrenceRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate

/**
 * Rebuilds the PLANNED supplement occurrence snapshot for a single date.
 *
 * This use case is the bridge from the existing schedule-resolution pipeline to the
 * new persisted planned-occurrence model.
 *
 * Current strategy:
 * - reuse the existing "supplements for date" schedule resolution flow
 * - convert each resolved schedule entry into one concrete planned occurrence row
 * - replace that date's occurrence snapshot in [SupplementOccurrenceRepository]
 *
 * Architectural intent:
 * - supplement_occurrences becomes the canonical PLANNED ledger
 * - supplement_daily_log remains the ACTUAL ledger
 * - logs may link back to planned rows by occurrenceId
 *
 * Important:
 * - one resolved entry becomes one planned occurrence row
 * - the same supplement may therefore appear multiple times on the same date
 * - occurrence IDs must be stable for deterministic planned↔actual reconciliation
 */
class MaterializeSupplementOccurrencesForDateUseCase @Inject constructor(
    private val getSupplementsWithUserSettingsForDateUseCase: GetSupplementsWithUserSettingsForDateUseCase,
    private val occurrenceRepository: SupplementOccurrenceRepository
) {

    suspend operator fun invoke(
        date: LocalDate
    ) {
        val supplementsForDate =
            getSupplementsWithUserSettingsForDateUseCase(date).first()

        val occurrences =
            supplementsForDate.flatMap { supplementWithSettings ->
                supplementWithSettings.resolvedScheduleEntries.map { entry ->
                    SupplementOccurrenceEntity(
                        id = entry.occurrenceId ?: buildFallbackOccurrenceId(
                            date = date,
                            supplementId = supplementWithSettings.supplement.id,
                            scheduleId = entry.scheduleId,
                            plannedTimeSeconds = entry.time.toSecondOfDay(),
                            sourceRowId = entry.sourceRowId,
                            sortOrder = entry.sortOrder
                        ),
                        supplementId = supplementWithSettings.supplement.id,
                        scheduleId = entry.scheduleId,
                        date = date.toString(),
                        plannedTimeSeconds = entry.time.toSecondOfDay(),
                        sourceType = SupplementOccurrenceSourceType.SCHEDULED,
                        isDeleted = false
                    )
                }
            }

        occurrenceRepository.replaceOccurrencesForDate(
            date = date,
            occurrences = occurrences
        )
    }

    /**
     * Transitional deterministic fallback for schedule paths that may still emit
     * a null occurrenceId during migration.
     *
     * This keeps planned occurrence materialization stable while the rest of the
     * scheduling pipeline is migrated toward always emitting occurrence IDs.
     */
    private fun buildFallbackOccurrenceId(
        date: LocalDate,
        supplementId: Long,
        scheduleId: Long?,
        plannedTimeSeconds: Int,
        sourceRowId: Long?,
        sortOrder: Int
    ): String {
        return listOf(
            date.toString(),
            supplementId.toString(),
            scheduleId?.toString() ?: "ns",
            sourceRowId?.toString() ?: "nr",
            plannedTimeSeconds.toString(),
            sortOrder.toString()
        ).joinToString(separator = "|")
    }
}