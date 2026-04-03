package com.example.hastanghubaga.domain.repository.supplement

import com.example.hastanghubaga.data.local.entity.supplement.SupplementOccurrenceEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

/**
 * Planned-occurrence repository for supplement timeline/planner rows.
 *
 * This repository represents the PLANNED side of the supplement system.
 *
 * Canonical responsibilities:
 * - persist concrete supplement occurrences for a specific date
 * - observe planned occurrences for timeline-style consumers
 * - rebuild a day snapshot when schedule inputs change
 * - support ad-hoc occurrence creation for extra/manual dose flows
 *
 * Important separation:
 * - planned occurrences live here
 * - actual logged doses live in [SupplementDoseLogRepository] / [SupplementDoseLogReadRepository]
 *
 * This preserves the distinction between:
 * - what the app planned
 * - what the user actually logged
 */
interface SupplementOccurrenceRepository {

    /**
     * Observe all non-deleted planned supplement occurrences for a date.
     *
     * Returned rows should be ordered chronologically for timeline consumers.
     */
    fun observeOccurrencesForDate(
        date: LocalDate
    ): Flow<List<SupplementOccurrenceEntity>>

    /**
     * Non-reactive read of all non-deleted planned supplement occurrences for a date.
     */
    suspend fun getOccurrencesForDate(
        date: LocalDate
    ): List<SupplementOccurrenceEntity>

    /**
     * Replace the planned supplement occurrences for a date with the supplied rows.
     *
     * Intended for day-snapshot rebuild flows:
     * - resolve valid occurrences for the date
     * - write one planned row per occurrence
     *
     * Implementations may rebuild by deleting/reinserting or by a more incremental strategy,
     * but the observable end result must match [occurrences].
     */
    suspend fun replaceOccurrencesForDate(
        date: LocalDate,
        occurrences: List<SupplementOccurrenceEntity>
    )

    /**
     * Insert a single ad-hoc planned occurrence.
     *
     * This supports manual / force-log workflows where an extra dose should become
     * a first-class planner/timeline item rather than only a floating actual log.
     */
    suspend fun insertOccurrence(
        occurrence: SupplementOccurrenceEntity
    )

    /**
     * Soft-delete a single planned occurrence.
     *
     * This preserves historical linkage for actual logs while removing the
     * occurrence from active timeline queries.
     */
    suspend fun softDeleteOccurrence(
        occurrenceId: String
    )
}