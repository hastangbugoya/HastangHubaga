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
 *
 * Planned logging contract:
 * - a non-null occurrenceId identifies one specific planned occurrence
 * - saving a log for the same non-null occurrenceId must update/replace the
 *   existing log row rather than create a duplicate
 * - a null occurrenceId represents an ad-hoc / force-logged activity and is
 *   inserted as an independent row
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
        title: String,
        activityType: ActivityType,
        startTimestamp: Long,
        endTimestamp: Long?,
        notes: String?,
        intensity: Int?,
        savedAddressId: Long?,
        addressAsRawString: String?,
        addressDisplayText: String?
    ): Long {
        Log.d(
            "ACTIVITY_RECON",
            "repo save activityId=$activityId occurrenceId=$occurrenceId title=$title activityType=$activityType savedAddressId=$savedAddressId addressAsRawString=$addressAsRawString addressDisplayText=$addressDisplayText"
        )

        val entity = ActivityLogEntity(
            activityId = activityId,
            occurrenceId = occurrenceId,
            title = title,
            activityType = activityType,
            startTimestamp = startTimestamp,
            endTimestamp = endTimestamp,
            notes = notes,
            intensity = intensity,
            savedAddressId = savedAddressId,
            addressAsRawString = addressAsRawString,
            addressDisplayText = addressDisplayText
        )

        return if (occurrenceId.isNullOrBlank()) {
            dao.insertActivityLog(entity)
        } else {
            dao.upsertActivityLogByOccurrenceId(entity)
        }
    }
}