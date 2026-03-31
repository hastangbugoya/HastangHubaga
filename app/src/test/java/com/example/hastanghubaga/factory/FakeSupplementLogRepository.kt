package com.example.hastanghubaga.factory

import com.example.hastanghubaga.data.local.entity.supplement.SupplementDoseUnit
import com.example.hastanghubaga.domain.repository.supplement.SupplementDoseLogRepository
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

/**
 * Test fake for [SupplementDoseLogRepository].
 *
 * This fake records all `logDose(...)` invocations so tests can verify:
 * - which supplement was logged
 * - which date/time was resolved
 * - what amount/unit was used
 * - whether the log was linked to a concrete occurrence
 *
 * The fake is intentionally simple:
 * - no validation
 * - no persistence
 * - no side effects beyond recording calls
 */
class FakeSupplementLogRepository : SupplementDoseLogRepository {

    /**
     * Captured invocation of [logDose].
     */
    data class Call(
        val supplementId: Long,
        val date: LocalDate,
        val time: LocalTime,
        val fractionTaken: Double,
        val unit: SupplementDoseUnit,
        val occurrenceId: String?
    )

    val calls = mutableListOf<Call>()

    override suspend fun logDose(
        supplementId: Long,
        date: LocalDate,
        time: LocalTime,
        fractionTaken: Double,
        doseUnit: SupplementDoseUnit,
        occurrenceId: String?
    ) {
        calls += Call(
            supplementId = supplementId,
            date = date,
            time = time,
            fractionTaken = fractionTaken,
            unit = doseUnit,
            occurrenceId = occurrenceId
        )
    }
}