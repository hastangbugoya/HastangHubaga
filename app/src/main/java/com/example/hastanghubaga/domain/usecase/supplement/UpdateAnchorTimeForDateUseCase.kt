package com.example.hastanghubaga.domain.usecase.supplement

import com.example.hastanghubaga.data.local.dao.supplement.EventTimeDao
import com.example.hastanghubaga.data.local.entity.supplement.DoseAnchorType
import com.example.hastanghubaga.data.local.entity.supplement.EventDailyOverrideEntity
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

class UpdateAnchorTimeForDateUseCase @Inject constructor(
    private val eventTimeDao: EventTimeDao
) {

    suspend operator fun invoke(
        anchor: DoseAnchorType,
        date: LocalDate,
        newTime: LocalTime
    ) {
        eventTimeDao.upsertDailyOverride(
            EventDailyOverrideEntity(
                anchor = anchor,
                date = date.toString(),
                timeSeconds = newTime.toSecondOfDay()
            )
        )
    }
}
