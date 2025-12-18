package com.example.hastanghubaga.data.local.mappers

import com.example.hastanghubaga.data.local.entity.activity.ActivityEntity
import com.example.hastanghubaga.domain.model.activity.Activity
import com.example.hastanghubaga.domain.time.TimePolicy
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

fun ActivityEntity.toDomain(): Activity =
    Activity(
        id = id,
        type = type,
        start = TimePolicy.utcMillisToLocalDateTime(startTimestamp),
        end = endTimestamp?. let {TimePolicy.utcMillisToLocalDateTime(endTimestamp)},
        notes = notes
    )

fun Activity.toEntity(): ActivityEntity =
    ActivityEntity(
        id = id,
        type = type,
        startTimestamp = TimePolicy.localDateTimeToUtcMillis(start),
        endTimestamp = end?. let {
            TimePolicy.localDateTimeToUtcMillis(end)
                                 },
        notes = notes
    )

