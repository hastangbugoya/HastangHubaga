package com.example.hastanghubaga.domain.usecase.supplement

import com.example.hastanghubaga.data.local.dao.supplement.SupplementEntityDao
import com.example.hastanghubaga.data.local.entity.supplement.SupplementEntity
import com.example.hastanghubaga.domain.model.supplement.Supplement
import com.example.hastanghubaga.domain.repository.supplement.SupplementRepository
import java.time.LocalDate
import javax.inject.Inject

/**
 * Use case responsible for retrieving supplements that should be shown for a given day.
 *
 * ## Purpose
 * This use case serves as the entry point for any screen or feature that needs to
 * determine *which supplements apply on a specific date*.
 *
 * Right now it returns all **active supplements** ordered by offset time, but later it
 * will incorporate:
 *
 * - Scheduling rules (DAILY, WEEKLY, EVERY_X_DAYS)
 * - Dose anchor logic (BREAKFAST, WAKEUP, etc.)
 * - User overrides for preferred dosing times
 * - Inactive supplements filtering
 * - Predictive next-dose logic
 *
 * Centralizing this logic ensures the UI never needs to understand scheduling rules
 * or business constraints — it simply calls the use case.
 *
 * ## Roadmap / Future Expansion
 * This use case will eventually:
 * - Accept a second parameter (e.g., current time) for time-based filtering.
 * - Combine supplement entities with user settings.
 * - Return domain models instead of Room entities.
 * - Integrate more advanced dose scheduling logic.
 *
 * ## Parameters
 * @param supplementDao DAO for reading supplement data from the Room database.
 *
 * ## Usage
 * ```
 * val todaysSupplements = getSupplementsForDateUseCase(LocalDate.now())
 * ```
 *
 * @return A list of supplements applicable for the provided date.
 */
class GetSupplementsForDateUseCase @Inject constructor(
    private val supplementRepository: SupplementRepository
) {

    /**
     * Executes the use case.
     *
     * @param date The date for which supplements should be retrieved.
     * @return A list of `Supplement` items active on that date.
     */
    suspend operator fun invoke(date: LocalDate): List<Supplement> {
        // TODO: Apply full scheduling logic here
        return supplementRepository.getActiveSupplementsOrderedByOffset()
    }
}
// ------------------------------------------------------------
// SCHEDULING LOGIC
//
// Rules are evaluated in this order:
// 1. Is the supplement active?
// 2. Should it be taken today?
// 3. What anchor applies?
// 4. Is there a daily override?
// 5. Is there a default anchor time?
// 6. Apply offset minutes
// ------------------------------------------------------------

