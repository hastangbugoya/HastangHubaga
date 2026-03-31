package com.example.hastanghubaga.data.local.mappers

import com.example.hastanghubaga.data.local.entity.activity.ActivityEntity
import com.example.hastanghubaga.data.time.JavaTimeAdapter
import com.example.hastanghubaga.domain.model.activity.Activity

fun ActivityEntity.toDomain(): Activity =
    Activity(
        id = id,
        type = type,
        start = JavaTimeAdapter.utcMillisToDomainLocalDateTime(startTimestamp),
        end = endTimestamp?.let(JavaTimeAdapter::utcMillisToDomainLocalDateTime),
        notes = notes,
        intensity = intensity,
        isWorkout = isWorkout,
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
        sendAlert = sendAlert,
        alertOffsetMinutes = alertOffsetMinutes
    )