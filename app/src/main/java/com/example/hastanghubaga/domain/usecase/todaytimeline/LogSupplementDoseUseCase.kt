package com.example.hastanghubaga.domain.usecase.todaytimeline

import com.example.hastanghubaga.domain.model.timeline.LogDoseInput
import com.example.hastanghubaga.domain.repository.supplement.SupplementDoseLogRepository
import com.example.hastanghubaga.domain.time.DomainTimePolicy
import com.example.hastanghubaga.domain.time.TimeUseIntent
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import javax.inject.Inject

/**
 * Logs a confirmed supplement intake to persistent storage.
 *
 * This use case represents a **command** in the domain layer.
 *
 * ## Occurrence-aware logging
 *
 * A supplement log may optionally be linked to a concrete planned occurrence.
 *
 * IMPORTANT BEHAVIOR:
 * - [occurrenceId] links the log to a planned timeline row
 * - The actual logged [time] is resolved from [TimeUseIntent]
 * - The log time MAY differ from the planned occurrence time
 *
 * This allows:
 * - late logging
 * - early intake
 * - real-world deviation from schedule
 *
 * The system must preserve BOTH:
 * - planned occurrence identity
 * - actual intake time
 */
class LogSupplementDoseUseCase @Inject constructor(
    private val repository: SupplementDoseLogRepository,
    private val clock: Clock
) {

    /**
     * Validates and persists a supplement intake record.
     *
     * Rules enforced:
     * - Dose must be positive
     * - Time is resolved from user intent
     * - OccurrenceId is passed through unchanged
     *
     * Future extension point:
     * - occurrence reconciliation (e.g., mark occurrence as taken)
     */
    suspend operator fun invoke(input: LogDoseInput) {
        require(input.fractionTaken > 0) {
            "Supplement dose amount must be greater than zero"
        }

        val (date, time) = resolveIntent(input.timeUseIntent)

        // IMPORTANT:
        // We intentionally allow occurrenceId + different actual time.
        // This reflects real-world behavior and preserves user truth.
        repository.logDose(
            supplementId = input.supplementId,
            date = date,
            time = time,
            fractionTaken = input.fractionTaken,
            doseUnit = input.unit,
            occurrenceId = input.occurrenceId
        )
    }

    private fun resolveIntent(
        intent: TimeUseIntent
    ): Pair<LocalDate, LocalTime> =
        DomainTimePolicy.resolveIntent(
            intent = intent,
            clock = clock
        )
}