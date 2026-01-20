package com.example.hastanghubaga.data.local.mappers

import com.example.hastanghubaga.data.local.entity.user.UpcomingScheduleEntity
import com.example.hastanghubaga.data.time.JavaTimeAdapter
import com.example.hastanghubaga.ui.timeline.TimelineItem
import com.example.hastanghubaga.domain.model.timeline.UpcomingSchedule
import com.example.hastanghubaga.ui.timeline.TodayUiRowType
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

fun TimelineItem.toUpcomingSchedule(
    date: LocalDate
): UpcomingSchedule? {
    return when (this) {
        is TimelineItem.SupplementTimelineItem ->
            UpcomingSchedule(
                type = TodayUiRowType.SUPPLEMENT,
                referenceId = supplement.supplement.id,
                scheduledAt =  LocalDateTime(
                    date = date,
                    time = this.time
                ),
                title = supplement.supplement.name,
                subtitle = "later"
            )

        is TimelineItem.MealTimelineItem ->
            UpcomingSchedule(
                type = TodayUiRowType.MEAL,
                referenceId = meal.id,
                scheduledAt =  LocalDateTime(
                    date = date,
                    time = this.time
                ),
                title = meal.type.name,
                subtitle = meal.notes
            )

        is TimelineItem.ActivityTimelineItem ->
            UpcomingSchedule(
                type = TodayUiRowType.ACTIVITY,
                referenceId = activity.id,
                scheduledAt =  LocalDateTime(
                    date = date,
                    time = this.time
                ),
                title = activity.type.name,
                subtitle = "later"
            )

        is TimelineItem.SupplementDoseLogTimelineItem -> null

        else -> null
    }
}

// data/mapper/UpcomingScheduleMappers.kt
fun UpcomingSchedule.toEntity(): UpcomingScheduleEntity =
    UpcomingScheduleEntity(
        id = id,
        type = type,
        referenceId = referenceId,
        scheduledAt = JavaTimeAdapter.domainLocalDateTimeToUtcMillis(scheduledAt),
        title = title,
        subtitle = subtitle,
        isCompleted = isCompleted
    )

fun UpcomingScheduleEntity.toDomain(): UpcomingSchedule =
    UpcomingSchedule(
        id = id,
        type = type,
        referenceId = referenceId,
        scheduledAt = JavaTimeAdapter.utcMillisToDomainLocalDateTime(scheduledAt),
        title = title,
        subtitle = subtitle,
        isCompleted = isCompleted
    )
