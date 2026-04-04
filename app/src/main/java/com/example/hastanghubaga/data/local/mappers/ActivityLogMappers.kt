package com.example.hastanghubaga.data.local.mappers

import com.example.hastanghubaga.data.local.entity.activity.ActivityLogEntity
import com.example.hastanghubaga.data.time.JavaTimeAdapter
import com.example.hastanghubaga.domain.model.activity.ActivityLog

/**
 * Mappers for actual logged activity sessions.
 *
 * Canonical activity model:
 * - ActivityEntity = template
 * - ActivityOccurrenceEntity = planned occurrence
 * - ActivityLogEntity = actual performed session
 */
fun ActivityLogEntity.toDomain(): ActivityLog =
    ActivityLog(
        id = id,
        activityId = activityId,
        occurrenceId = occurrenceId,
        activityType = activityType,
        start = JavaTimeAdapter.utcMillisToDomainLocalDateTime(startTimestamp),
        end = endTimestamp?.let(JavaTimeAdapter::utcMillisToDomainLocalDateTime),
        notes = notes,
        intensity = intensity
    )

fun ActivityLog.toEntity(): ActivityLogEntity =
    ActivityLogEntity(
        id = id,
        activityId = activityId,
        occurrenceId = occurrenceId,
        activityType = activityType,
        startTimestamp = JavaTimeAdapter.domainLocalDateTimeToUtcMillis(start),
        endTimestamp = end?.let(JavaTimeAdapter::domainLocalDateTimeToUtcMillis),
        notes = notes,
        intensity = intensity
    )

