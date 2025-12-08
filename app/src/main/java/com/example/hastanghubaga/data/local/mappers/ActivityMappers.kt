package com.example.hastanghubaga.data.local.mappers

import com.example.hastanghubaga.data.local.entity.activity.ActivityEntity
import com.example.hastanghubaga.domain.model.Activity
import com.example.hastanghubaga.domain.model.ActivityType
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

fun ActivityEntity.toDomain(): Activity =
    Activity(
        id = id,
        type = type,
        start = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(startTimestamp),
            ZoneId.systemDefault()
        ),
        end = endTimestamp?.let {
            LocalDateTime.ofInstant(
                Instant.ofEpochMilli(it),
                ZoneId.systemDefault()
            )
        },
        notes = notes
    )

fun Activity.toEntity(): ActivityEntity =
    ActivityEntity(
        id = id,
        type = type,
        startTimestamp = start.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
        endTimestamp = end?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli(),
        notes = notes
    )

