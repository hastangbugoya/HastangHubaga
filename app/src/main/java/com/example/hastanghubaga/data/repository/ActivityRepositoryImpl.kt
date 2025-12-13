package com.example.hastanghubaga.data.repository

import com.example.hastanghubaga.data.local.dao.activity.ActivityEntityDao
import com.example.hastanghubaga.data.local.mappers.toSupplementSettings
import com.example.hastanghubaga.data.local.mappers.toEntity
import com.example.hastanghubaga.domain.model.activity.Activity
import com.example.hastanghubaga.domain.repository.activity.ActivityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ActivityRepositoryImpl @Inject constructor(
    private val dao: ActivityEntityDao
) : ActivityRepository {

    override fun observeAll(): Flow<List<Activity>> =
        dao.observeAllActivities().map { list -> list.map { it.toSupplementSettings() } }

    override fun observeActivity(id: Long): Flow<Activity?> =
        dao.observeActivity(id).map { it?.toSupplementSettings() }

    override suspend fun addActivity(activity: Activity): Long =
        dao.insertActivity(activity.toEntity())

    override suspend fun deleteActivity(activity: Activity) =
        dao.deleteActivity(activity.toEntity())

    /*
    When logging workout:
    → adjust supplement schedule
    → shift predicted dose
    → trigger reminders
     */
}
