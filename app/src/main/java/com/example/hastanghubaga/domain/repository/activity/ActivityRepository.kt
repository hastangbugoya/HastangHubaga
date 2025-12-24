package com.example.hastanghubaga.domain.repository.activity

import com.example.hastanghubaga.domain.model.activity.Activity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface ActivityRepository {

    fun observeAll(): Flow<List<Activity>>

    fun observeActivity(id: Long): Flow<Activity?>

    suspend fun addActivity(activity: Activity): Long

    suspend fun deleteActivity(activity: Activity)

    fun observeActivitiesForDate(date: LocalDate): Flow<List<Activity>>
}