package com.example.hastanghubaga.domain.usecase.supplement

import com.example.hastanghubaga.domain.repository.supplement.SupplementRepository
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

class StartDay @Inject constructor(
    private val supplementRepository: SupplementRepository
) {
    suspend operator fun invoke(date: LocalDate, time: LocalTime) {
        return supplementRepository.setHourZero(date, time)
    }
}