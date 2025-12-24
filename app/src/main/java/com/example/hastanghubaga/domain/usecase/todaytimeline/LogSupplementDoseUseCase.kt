package com.example.hastanghubaga.domain.usecase.todaytimeline

import com.example.hastanghubaga.domain.model.timeline.LogDoseInput
import com.example.hastanghubaga.domain.repository.supplement.SupplementDoseLogRepository
import com.example.hastanghubaga.domain.time.DomainTimePolicy
import com.example.hastanghubaga.domain.time.TimeUseIntent
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject


/**
 * Logs a confirmed supplement intake to persistent storage.
 *
 * This use case represents a **command** in the domain layer:
 * it validates user-provided input and delegates persistence
 * to the data layer.
 *
 * ## Responsibilities
 * - Validate user input (amount, unit)
 * - Enforce domain rules around supplement intake logging
 * - Decide *what* should be persisted
 * - Delegate *how* it is persisted to a repository
 *
 * ## Validation vs Persistence
 * This class owns **validation and intent**.
 *
 * The repository owns **storage mechanics**.
 *
 * Specifically:
 * - This use case decides *whether* an intake is valid
 * - The repository decides *how* it is stored (Room, SQL, upsert rules)
 *
 * This separation ensures:
 * - Business rules are testable without a database
 * - Persistence can change without rewriting validation
 *
 * ## What this use case does NOT do
 * - It does not show UI
 * - It does not format display data
 * - It does not manage UI state or effects
 * - It does not decide *when* the user is prompted
 *
 * Those concerns belong to the ViewModel and UI layers.
 *
 * ## Invocation
 * This use case uses `operator fun invoke()` because:
 * - It performs a state-changing action
 * - It represents a domain command
 * - It may be executed asynchronously
 *
 * @see HandleTimelineItemTapUseCase
 * @see TimelineTapAction.RequestDoseInput
 */
class LogSupplementDoseUseCase @Inject constructor(
    private val repository: SupplementDoseLogRepository,
    private val clock: Clock
) {

    /**
     * Validates and persists a supplement intake record.
     *
     * @param input
     * The confirmed dose information provided by the user.`
     * This input is assumed to come from explicit user confirmation
     * (e.g. a dose input dialog).
     *
     * @throws IllegalArgumentException
     * If the dose amount is zero or negative.
     */
    suspend operator fun invoke(input: LogDoseInput) {
        require(input.fractionTaken > 0) {
            "Supplement dose amount must be greater than zero"
        }

        val (date, time) = resolveIntent(input.timeUseIntent)
        repository.logDose(
            supplementId = input.supplementId,
            date = date,
            time = time,
            fractionTaken = input.fractionTaken,
            doseUnit = input.unit,
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
