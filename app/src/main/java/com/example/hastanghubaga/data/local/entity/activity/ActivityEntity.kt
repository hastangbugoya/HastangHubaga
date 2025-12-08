package com.example.hastanghubaga.data.local.entity.activity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.hastanghubaga.domain.model.ActivityType

@Entity(tableName = "activities")
data class ActivityEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val type: ActivityType,

    // store epoch millis for timezone-safe history
    val startTimestamp: Long,
    val endTimestamp: Long?,

    val notes: String? = null
)
