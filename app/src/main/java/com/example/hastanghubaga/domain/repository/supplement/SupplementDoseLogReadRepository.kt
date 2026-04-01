package com.example.hastanghubaga.domain.repository.supplement

import com.example.hastanghubaga.domain.model.supplement.SupplementDoseLog
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

/**
 * Read-side repository for actual logged supplement intake events.
 *
 * This is intentionally separate from:
 * - [SupplementRepository], which focuses on planned/scheduled supplement behavior
 * - [SupplementDoseLogRepository], which focuses on write-side logging commands
 *
 * WHY THIS EXISTS
 * - Planned supplement occurrences and actual logged doses are different concepts
 * - A logged dose may or may not belong to a schedule rule
 * - Force-logged doses must remain valid first-class actual events
 * - Timeline/reporting layers need a clean read model for actual dose history
 *
 * IMPORTANT
 * - This repository should not impose schedule rules
 * - It should return what the user actually logged
 * - Reconciliation with planned schedule behavior can remain optional/incremental
 */
interface SupplementDoseLogReadRepository {

    /**
     * Observes all actual supplement dose logs for a given date.
     *
     * Returned items represent real logged events for that day, including:
     * - scheduled doses
     * - extra/off-plan doses
     * - make-up doses
     */
    fun observeDoseLogsForDate(
        date: LocalDate
    ): Flow<List<SupplementDoseLog>>
}