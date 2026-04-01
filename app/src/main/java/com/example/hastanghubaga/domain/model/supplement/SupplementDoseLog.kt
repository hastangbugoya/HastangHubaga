package com.example.hastanghubaga.domain.model.supplement

import com.example.hastanghubaga.data.local.entity.supplement.SupplementDoseUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

/**
 * Domain model representing one actual logged supplement intake event.
 *
 * This is an HH-native domain model, not a Room entity.
 *
 * IMPORTANT
 * - This is an ACTUAL log, not a planned schedule entry
 * - A dose log may or may not belong to a scheduling rule
 * - A dose log may represent:
 *   - a scheduled dose
 *   - an extra/off-plan dose
 *   - a make-up dose
 *
 * The app should not treat off-schedule logs as invalid. They are user-declared
 * events that happened. Reconciliation with planned schedule behavior can remain
 * optional and incremental.
 *
 * [occurrenceId] is optional because some logs may link to a concrete planned
 * occurrence while others are manual/force-logged entries.
 */
data class SupplementDoseLog(
    val id: Long,
    val supplementId: Long,
    val date: LocalDate,
    val actualServingTaken: Double,
    val doseUnit: SupplementDoseUnit,
    val timestamp: LocalDateTime,
    val occurrenceId: String? = null
)