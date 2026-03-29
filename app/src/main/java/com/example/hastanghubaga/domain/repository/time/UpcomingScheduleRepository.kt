package com.example.hastanghubaga.domain.repository.time

import com.example.hastanghubaga.domain.model.timeline.UpcomingSchedule
import kotlinx.coroutines.flow.Flow

interface UpcomingScheduleRepository {
    fun observeUpcoming(): Flow<List<UpcomingSchedule>>
    suspend fun replaceAll(items: List<UpcomingSchedule>)

    fun observeNextUpcoming(): Flow<UpcomingSchedule?>
}