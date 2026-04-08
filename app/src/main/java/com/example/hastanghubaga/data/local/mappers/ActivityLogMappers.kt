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
 *
 * Important:
 * - title/type/address fields are treated as historical snapshots on the log
 * - timeline/history should be able to render the actual logged context without
 *   needing to re-resolve the current template or saved address row
 */
fun ActivityLogEntity.toDomain(): ActivityLog =
    ActivityLog(
        id = id,
        activityId = activityId,
        occurrenceId = occurrenceId,
        title = title,
        activityType = activityType,
        start = JavaTimeAdapter.utcMillisToDomainLocalDateTime(startTimestamp),
        end = endTimestamp?.let(JavaTimeAdapter::utcMillisToDomainLocalDateTime),
        notes = notes,
        intensity = intensity,
        savedAddressId = savedAddressId,
        addressAsRawString = addressAsRawString,
        addressDisplayText = addressDisplayText
    )

fun ActivityLog.toEntity(): ActivityLogEntity =
    ActivityLogEntity(
        id = id,
        activityId = activityId,
        occurrenceId = occurrenceId,
        title = title,
        activityType = activityType,
        startTimestamp = JavaTimeAdapter.domainLocalDateTimeToUtcMillis(start),
        endTimestamp = end?.let(JavaTimeAdapter::domainLocalDateTimeToUtcMillis),
        notes = notes,
        intensity = intensity,
        savedAddressId = savedAddressId,
        addressAsRawString = addressAsRawString,
        addressDisplayText = addressDisplayText
    )