package com.example.hastanghubaga.data.repository

import android.util.Log
import com.example.hastanghubaga.data.local.dao.timeline.UpcomingScheduleDao
import com.example.hastanghubaga.data.local.entity.user.UpcomingScheduleEntity
import com.example.hastanghubaga.data.local.mappers.toDomain
import com.example.hastanghubaga.data.local.mappers.toEntity
import com.example.hastanghubaga.domain.model.timeline.UpcomingSchedule
import com.example.hastanghubaga.domain.repository.time.UpcomingScheduleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpcomingScheduleRepositoryImpl @Inject constructor(
    private val dao: UpcomingScheduleDao
) : UpcomingScheduleRepository {

    override fun observeUpcoming(): Flow<List<UpcomingSchedule>> {
        Log.d("Meow", "UpcomingScheduleRepositoryImpl > observeUpcoming")
        return dao.observeAll()
            .map { list -> list.map { it.toDomain() } }
    }

    override suspend fun replaceAll(items: List<UpcomingSchedule>) {
        Log.d("Meow", "UpcomingScheduleRepositoryImpl > replaceAll > items: ${items.size}")
        dao.clearAll()
        dao.insertAll(items.map { it.toEntity() })
    }

    override fun observeNextUpcoming(): Flow<UpcomingSchedule?> {
        Log.d("Meow", "UpcomingScheduleRepositoryImpl > observeNextUpcoming")
        return dao.observeNextUpcoming(LocalDateTime.now())
            .map { it?.toDomain() }
    }

}
