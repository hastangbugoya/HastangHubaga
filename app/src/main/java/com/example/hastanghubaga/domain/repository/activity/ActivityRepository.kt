package com.example.hastanghubaga.domain.repository.activity

import com.example.hastanghubaga.domain.model.activity.Activity
import com.example.hastanghubaga.domain.model.activity.ActivityType
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface ActivityRepository {

    fun observeAll(): Flow<List<Activity>>

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