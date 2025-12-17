package com.example.hastanghubaga.data.repository

import com.example.hastanghubaga.data.local.dao.activity.ActivityEntityDao
import com.example.hastanghubaga.data.local.entity.activity.ActivityEntity
import com.example.hastanghubaga.data.local.mappers.toEntity
import com.example.hastanghubaga.data.local.mappers.toDomain
import com.example.hastanghubaga.domain.model.activity.Activity
import com.example.hastanghubaga.domain.repository.activity.ActivityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

class ActivityRepositoryImpl @Inject constructor(
    private val dao: ActivityEntityDao
) : ActivityRepository {

    override fun observeAll(): Flow<List<Activity>> =
        dao.observeAllActivities().map { list -> list.map { it.toDomain() } }

    override fun observeActivity(id: Long): Flow<Activity?> =
        dao.observeActivity(id).map { it?.toDomain() }

    override suspend fun addActivity(activity: Activity): Long =
        dao.insertActivity(activity.toEntity())

    override suspend fun deleteActivity(activity: Activity) =
        dao.deleteActivity(activity.toEntity())

    override fun observeActivitiesForDate(date: LocalDate): Flow<List<Activity>> {
        val zone = ZoneId.systemDefault()

        val start = date.atStartOfDay(zone).toInstant().toEpochMilli()
        val end = date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()

        return dao.observeActivitiesForDay(start, end)
            .map { it.map(ActivityEntity::toDomain) }
    }

    /*
    When logging workout:
    → adjust supplement schedule
    → shift predicted dose
    → trigger reminders
     */
}
