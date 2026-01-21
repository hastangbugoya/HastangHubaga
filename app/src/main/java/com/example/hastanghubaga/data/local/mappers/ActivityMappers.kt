package com.example.hastanghubaga.data.local.mappers

import com.example.hastanghubaga.data.local.entity.activity.ActivityEntity
import com.example.hastanghubaga.data.time.JavaTimeAdapter
import com.example.hastanghubaga.domain.model.activity.Activity
import com.example.hastanghubaga.domain.time.DomainTimePolicy
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

fun ActivityEntity.toDomain(): Activity =
    Activity(
        id = id,
        type = type,
        start = JavaTimeAdapter.utcMillisToDomainLocalDateTime(startTimestamp),
        end = endTimestamp?.let(JavaTimeAdapter::utcMillisToDomainLocalDateTime),
        notes = notes,
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
        sendAlert = sendAlert,
        alertOffsetMinutes = alertOffsetMinutes
    )

