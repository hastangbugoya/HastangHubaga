package com.example.hastanghubaga.testing

import com.example.hastanghubaga.domain.model.timeline.UpcomingSchedule
import com.example.hastanghubaga.domain.repository.time.UpcomingScheduleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeUpcomingScheduleRepository(
    private val schedules: List<UpcomingSchedule> = emptyList()
) : UpcomingScheduleRepository {

//    override suspend fun getUpcomingSchedules(): List<UpcomingSchedule> {
//        return schedules
//    }

    override fun observeUpcoming(): Flow<List<UpcomingSchedule>> {
        return flowOf(schedules)
    }

    override suspend fun replaceAll(items: List<UpcomingSchedule>) {
        // no-op for tests
        // intentionally does nothing
    }

    override fun observeNextUpcoming(): Flow<UpcomingSchedule?> {
        return flowOf(schedules.minByOrNull { it.scheduledAt })
    }
}
