package com.example.hastanghubaga.alerts
sealed interface ScheduleResult {
    data object Scheduled : ScheduleResult
    data object PermissionRequired : ScheduleResult
}
