package com.example.hastanghubaga.domain.repository.supplement

import com.example.hastanghubaga.data.local.entity.supplement.SupplementDoseUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

interface SupplementDoseLogRepository {
    suspend fun logDose(
        supplementId: Long,
        date: LocalDate,
        time: LocalTime,
        fractionTaken: Double,
        doseUnit: SupplementDoseUnit
    )
}