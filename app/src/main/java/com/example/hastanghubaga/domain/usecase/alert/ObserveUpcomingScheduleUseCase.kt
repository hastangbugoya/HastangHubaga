package com.example.hastanghubaga.domain.usecase.alert

import com.example.hastanghubaga.domain.model.timeline.UpcomingSchedule
import com.example.hastanghubaga.domain.repository.time.UpcomingScheduleRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveUpcomingScheduleUseCase @Inject constructor(
    private val repo: UpcomingScheduleRepository
) {
    operator fun invoke(): Flow<List<UpcomingSchedule>> = repo.observeUpcoming()
}
