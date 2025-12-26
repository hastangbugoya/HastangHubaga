package com.example.hastanghubaga.widget.nextup

import android.util.Log
import com.example.hastanghubaga.domain.model.timeline.UpcomingSchedule
import com.example.hastanghubaga.domain.repository.time.UpcomingScheduleRepository
import com.example.hastanghubaga.domain.time.DomainTimePolicy
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import javax.inject.Inject

class ObserveNextUpcomingUseCase @Inject constructor(
    private val upcomingScheduleRepository: UpcomingScheduleRepository,
) {

    suspend operator fun invoke(): UpcomingSchedule? {
        Log.d("Meow","ObserveNextUpcomingUseCase > invoke")
        return upcomingScheduleRepository.observeNextUpcoming().firstOrNull()
    }
}