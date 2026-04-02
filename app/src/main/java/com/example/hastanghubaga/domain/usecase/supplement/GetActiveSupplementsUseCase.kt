package com.example.hastanghubaga.domain.usecase.supplement

import com.example.hastanghubaga.domain.model.supplement.Supplement
import com.example.hastanghubaga.domain.repository.supplement.SupplementRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

/**
 * Returns all active supplements.
 *
 * This use case is intentionally simple:
 * - it exposes active supplement choices for UI flows such as force-log
 * - it does NOT apply timeline/date scheduling rules
 * - it does NOT decide whether a supplement should appear on a specific day
 *
 * Important:
 * Force-log is an "actual happened" flow, not a planned-schedule flow.
 * The user may choose any active supplement even if it is not currently scheduled.
 */
class GetActiveSupplementsUseCase @Inject constructor(
    private val repository: SupplementRepository
) {

    operator fun invoke(): Flow<List<Supplement>> {
        return repository.getActiveSupplements()
    }
}