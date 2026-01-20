package com.example.hastanghubaga.domain.model.timeline

// domain/model/TimelineItem.kt
sealed interface TimelineItem {
    val sortInstant: kotlinx.datetime.Instant
    val stableKey: TimelineKey
}

sealed interface TimelineKey {
    data class ScheduledSupplement(val supplementId: Long, val scheduledAt: kotlinx.datetime.LocalDateTime) : TimelineKey
    data class Meal(val mealId: Long) : TimelineKey
    data class Activity(val activityId: Long) : TimelineKey
    data class SupplementDoseLog(val doseLogId: Long) : TimelineKey
}
