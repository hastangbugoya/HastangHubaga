package com.example.hastanghubaga.data.repository

import com.example.hastanghubaga.data.local.dao.timeline.UpcomingScheduleDao
import com.example.hastanghubaga.data.local.entity.user.UpcomingScheduleEntity
import com.example.hastanghubaga.data.local.mappers.toDomain
import com.example.hastanghubaga.data.local.mappers.toEntity
import com.example.hastanghubaga.domain.model.timeline.UpcomingSchedule
import com.example.hastanghubaga.domain.repository.time.UpcomingScheduleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpcomingScheduleRepositoryImpl @Inject constructor(
    private val dao: UpcomingScheduleDao
) : UpcomingScheduleRepository {

    override fun observeUpcoming(): Flow<List<UpcomingSchedule>> =
        dao.observeAll()
            .map { list -> list.map { it.toDomain() } }

    override suspend fun replaceAll(items: List<UpcomingSchedule>) {
        dao.clearAll()
        dao.insertAll(items.map { it.toEntity() })
    }
}
