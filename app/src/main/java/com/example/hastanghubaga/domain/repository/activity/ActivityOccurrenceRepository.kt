package com.example.hastanghubaga.domain.repository.activity

import com.example.hastanghubaga.data.local.entity.activity.ActivityOccurrenceEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

/**
 * Planned-occurrence repository for activity timeline/planner rows.
 *
 * This repository represents the PLANNED side of the activity system.
 *
 * Canonical responsibilities:
 * - persist concrete activity occurrences for a specific date
 * - observe planned occurrences for timeline-style consumers
 * - rebuild a day snapshot when schedule inputs change
 * - support ad-hoc occurrence creation for extra/manual activity flows
 * - support per-occurrence workout-flag overrides used by the planner
 *
 * Important separation:
 * - planned occurrences live here
 * - actual activity logs live in ActivityLogRepository (future)
 *
 * This preserves the distinction between:
 * - what the app planned
 * - what the user actually did
 */
interface ActivityOccurrenceRepository {

    /**
     * Observe all non-deleted planned activity occurrences for a date.
     *
     * Returned rows should be ordered chronologically for timeline consumers.
     */
    fun observeOccurrencesForDate(
        date: LocalDate
    ): Flow<List<ActivityOccurrenceEntity>>

    /**
     * Non-reactive read of all non-deleted planned activity occurrences for a date.
     */
    suspend fun getOccurrencesForDate(
        date: LocalDate
    ): List<ActivityOccurrenceEntity>

    /**
     * Non-reactive read of all non-deleted workout-capable planned activity
     * occurrences for a date.
     *
     * This supports planner/anchor flows that need occurrence-level workout
     * sources after template defaults have been materialized and possibly
     * overridden per occurrence.
     */
    suspend fun getWorkoutOccurrencesForDate(
        date: LocalDate
    ): List<ActivityOccurrenceEntity>

    /**
     * Replace the planned activity occurrences for a date with the supplied rows.
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
        occurrences: List<ActivityOccurrenceEntity>
    )

    /**
     * Insert a single ad-hoc planned occurrence.
     *
     * This supports manual workflows where an extra activity should become
     * a first-class planner/timeline item rather than only a floating log.
     */
    suspend fun insertOccurrence(
        occurrence: ActivityOccurrenceEntity
    )

    /**
     * Soft-delete a single planned occurrence.
     *
     * This preserves historical linkage for future logs while removing the
     * occurrence from active timeline queries.
     */
    suspend fun softDeleteOccurrence(
        occurrenceId: String
    )

    /**
     * Updates the occurrence-level workout flag.
     *
     * Canonical rule:
     * - ActivityEntity.isWorkout is only the template default
     * - ActivityOccurrenceEntity.isWorkout is the day-level planner snapshot
     * - this method applies the per-occurrence override without mutating the template
     */
    suspend fun updateOccurrenceWorkoutFlag(
        occurrenceId: String,
        isWorkout: Boolean
    )
}