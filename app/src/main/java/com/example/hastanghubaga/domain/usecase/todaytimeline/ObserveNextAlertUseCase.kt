package com.example.hastanghubaga.domain.usecase.todaytimeline

import com.example.hastanghubaga.domain.model.timeline.UpcomingSchedule
import com.example.hastanghubaga.domain.repository.time.UpcomingScheduleRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveNextAlertUseCase @Inject constructor(
    private val repo: UpcomingScheduleRepository
) {
    operator fun invoke(): Flow<UpcomingSchedule?> =
        repo.observeNextUpcoming()
}
