package com.example.hastanghubaga.domain.repository.activity

import com.example.hastanghubaga.domain.model.Activity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface ActivityRepository {

    fun observeAll(): Flow<List<Activity>>

    fun observeActivity(id: Long): Flow<Activity?>

    suspend fun addActivity(activity: Activity): Long

    suspend fun deleteActivity(activity: Activity)
}