package com.example.hastanghubaga.domain.repository.activity

import com.example.hastanghubaga.domain.model.activity.Activity
import com.example.hastanghubaga.domain.model.activity.ActivityType
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface ActivityRepository {

    fun observeAll(): Flow<List<Activity>>

    /**
     * Observe all active activity templates/rows that are eligible for manual
     * force-log selection.
     *
     * Purpose:
     * - Today screen force-log activity picker
     * - Future force-log meal-style consistency across scheduler-backed systems
     *
     * Important:
     * - This is intentionally NOT date-specific
     * - This should return active activities only
     * - This should NOT materialize or merge planned occurrences for a specific day
     *
     * This keeps force-log selection distinct from planned timeline items.
     */
    fun observeActiveActivities(): Flow<List<Activity>>

    fun observeActivity(id: Long): Flow<Activity?>

    suspend fun addActivity(activity: Activity): Long

    suspend fun deleteActivity(activity: Activity)

    fun observeActivitiesForDate(date: LocalDate): Flow<List<Activity>>

    suspend fun insertActivity(
        type: ActivityType,
        startTimestamp: Long,
        endTimestamp: Long?,
        notes: String?,
        intensity: Int?
    ): Long
}