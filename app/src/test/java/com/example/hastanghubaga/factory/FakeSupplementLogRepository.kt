package com.example.hastanghubaga.factory

import com.example.hastanghubaga.data.local.entity.supplement.SupplementDoseUnit
import com.example.hastanghubaga.domain.repository.supplement.SupplementDoseLogRepository
import com.example.hastanghubaga.domain.repository.supplement.SupplementRepository
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

class FakeSupplementLogRepository : SupplementDoseLogRepository {

    data class Call(
        val supplementId: Long,
        val date: LocalDate,
        val time: LocalTime,
        val fractionTaken: Double,
        val unit: SupplementDoseUnit
    )

    val calls = mutableListOf<Call>()

    override suspend fun logDose(
        supplementId: Long,
        date: LocalDate,
        time: LocalTime,
        fractionTaken: Double,
        doseUnit: SupplementDoseUnit
    ) {
        calls += Call(
            supplementId,
            date,
            time,
            fractionTaken,
            doseUnit
        )
    }

//    override suspend fun logDose(
//        supplementId: Long,
//        date: java.time.LocalDate,
//        time: java.time.LocalTime,
//        fractionTaken: Double,
//        doseUnit: SupplementDoseUnit
//    ) {
//        calls += Call(
//            supplementId,
//            date,
//            time,
//            fractionTaken,
//            doseUnit
//        )
//    }
}
