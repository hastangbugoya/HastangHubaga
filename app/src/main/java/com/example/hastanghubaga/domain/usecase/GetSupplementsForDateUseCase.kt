package com.example.hastanghubaga.domain.usecase

import com.example.hastanghubaga.data.local.dao.supplement.SupplementEntityDao
import com.example.hastanghubaga.data.local.entity.supplement.SupplementEntity
import java.time.LocalDate
import javax.inject.Inject

class GetSupplementsForDateUseCase @Inject constructor(
    private val supplementDao: SupplementEntityDao
) {
    suspend operator fun invoke(date: LocalDate): List<SupplementEntity> {
        // placeholder logic — later we filter by schedule rules
        return supplementDao.getActiveSupplementsOrderedByOffset()
    }
}
