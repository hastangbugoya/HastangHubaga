package com.example.hastanghubaga.data.repository

import android.util.Log
import com.example.hastanghubaga.data.local.dao.activity.ActivityEntityDao
import com.example.hastanghubaga.data.local.dao.activity.ActivityOccurrenceDao
import com.example.hastanghubaga.data.local.entity.activity.ActivityEntity
import com.example.hastanghubaga.data.local.mappers.toEntity
import com.example.hastanghubaga.data.local.mappers.toDomain
import com.example.hastanghubaga.domain.model.activity.Activity
import com.example.hastanghubaga.domain.model.activity.ActivityType
import com.example.hastanghubaga.domain.repository.activity.ActivityRepository
import com.example.hastanghubaga.domain.time.DomainTimePolicy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import javax.inject.Inject

class ActivityRepositoryImpl @Inject constructor(
    private val dao: ActivityEntityDao,
    private val occurrenceDao: ActivityOccurrenceDao
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
        val (start, end) = DomainTimePolicy.utcMillisRangeForLocalDate(date)

        val unscheduledFlow = dao.observeActivitiesForDay(start, end)

        val scheduledFlow =
            occurrenceDao.observeOccurrencesWithActivityForDate(date.toString())

        return combine(unscheduledFlow, scheduledFlow) { unscheduled, scheduled ->

            val unscheduledMapped = unscheduled.map { it.toDomain() }

            val scheduledMapped = scheduled.map { it.toDomain() }

            (unscheduledMapped + scheduledMapped)
                .sortedBy { it.start }
        }
    }

    override suspend fun insertActivity(
        type: ActivityType,
        startTimestamp: Long,
        endTimestamp: Long?,
        notes: String?,
        intensity: Int?
    ): Long {
        return dao.insertActivity(
            ActivityEntity(
                id = 0L, // auto-generate
                type = type,
                startTimestamp = startTimestamp,
                endTimestamp = endTimestamp,
                notes = notes,
                intensity = intensity,

                // 🔑 NEW: align with supplement behavior
                isActive = true
            )
        )
    }

    /*
    When logging workout:
    → adjust supplement schedule
    → shift predicted dose
    → trigger reminders
     */
}