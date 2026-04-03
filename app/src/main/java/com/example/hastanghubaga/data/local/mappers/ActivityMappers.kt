package com.example.hastanghubaga.data.local.mappers

import com.example.hastanghubaga.data.local.entity.activity.ActivityEntity
import com.example.hastanghubaga.data.local.models.ActivityOccurrenceWithActivity
import com.example.hastanghubaga.data.time.JavaTimeAdapter
import com.example.hastanghubaga.domain.model.activity.Activity
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime

fun ActivityEntity.toDomain(): Activity =
    Activity(
        id = id,
        type = type,
        start = JavaTimeAdapter.utcMillisToDomainLocalDateTime(startTimestamp),
        end = endTimestamp?.let(JavaTimeAdapter::utcMillisToDomainLocalDateTime),
        notes = notes,
        intensity = intensity,
        isWorkout = isWorkout,
        isActive = isActive,
        sendAlert = sendAlert,
        alertOffsetMinutes = alertOffsetMinutes
    )

fun Activity.toEntity(): ActivityEntity =
    ActivityEntity(
        id = id,
        type = type,
        startTimestamp = JavaTimeAdapter.domainLocalDateTimeToUtcMillis(start),
        endTimestamp = end?.let(JavaTimeAdapter::domainLocalDateTimeToUtcMillis),
        notes = notes,
        intensity = intensity,
        isWorkout = isWorkout,
        isActive = isActive,
        sendAlert = sendAlert,
        alertOffsetMinutes = alertOffsetMinutes
    )


fun ActivityOccurrenceWithActivity.toDomain(): Activity {
    val date = LocalDate.parse(occurrence.date)

    val seconds = occurrence.plannedTimeSeconds
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60

    val start = LocalDateTime(
        date = date,
        time = LocalTime(hour = hours, minute = minutes, second = secs)
    )

    return Activity(
        id = activity.id,
        type = activity.type,
        start = start,
        end = null,

        notes = activity.notes,
        intensity = activity.intensity,

        // 🔑 CRITICAL: occurrence overrides template
        isWorkout = occurrence.isWorkout,

        isActive = activity.isActive,
        sendAlert = activity.sendAlert,
        alertOffsetMinutes = activity.alertOffsetMinutes
    )
}