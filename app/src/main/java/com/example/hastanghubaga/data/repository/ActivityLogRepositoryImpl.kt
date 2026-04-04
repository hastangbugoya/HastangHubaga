package com.example.hastanghubaga.data.repository

import android.util.Log
import com.example.hastanghubaga.data.local.dao.activity.ActivityLogDao
import com.example.hastanghubaga.data.local.entity.activity.ActivityLogEntity
import com.example.hastanghubaga.data.local.mappers.toDomain
import com.example.hastanghubaga.domain.model.activity.ActivityLog
import com.example.hastanghubaga.domain.model.activity.ActivityType
import com.example.hastanghubaga.domain.repository.activity.ActivityLogRepository
import com.example.hastanghubaga.domain.time.DomainTimePolicy
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate

/**
 * Repository implementation for actual logged activity sessions.
 *
 * Canonical activity model:
 * - ActivityEntity = template
 * - ActivityOccurrenceEntity = planned occurrence
 * - ActivityLogEntity = actual performed session
 */
class ActivityLogRepositoryImpl @Inject constructor(
    private val dao: ActivityLogDao
) : ActivityLogRepository {

    override fun observeActivityLogsForDate(date: LocalDate): Flow<List<ActivityLog>> {
        val (start, end) = DomainTimePolicy.utcMillisRangeForLocalDate(date)

        return dao.observeActivityLogsForDay(
            startUtcMillis = start,
            endUtcMillis = end
        ).map { logs ->
            logs.map { it.toDomain() }
        }
    }

    override suspend fun insertActivityLog(
        activityId: Long?,
        occurrenceId: String?,
        activityType: ActivityType,
        startTimestamp: Long,
        endTimestamp: Long?,
        notes: String?,
        intensity: Int?
    ): Long {
        Log.d(
            "ACTIVITY_RECON",
            "repo insert activityId=$activityId occurrenceId=$occurrenceId activityType=$activityType"
        )
        return dao.insertActivityLog(
            ActivityLogEntity(
                activityId = activityId,
                occurrenceId = occurrenceId,
                activityType = activityType,
                startTimestamp = startTimestamp,
                endTimestamp = endTimestamp,
                notes = notes,
                intensity = intensity
            )
        )
    }
}