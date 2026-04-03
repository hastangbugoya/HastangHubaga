package com.example.hastanghubaga.data.local.entity.activity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalTime

@Entity(
    tableName = "activity_schedule_fixed_times",
    foreignKeys = [
        ForeignKey(
            entity = ActivityScheduleEntity::class,
            parentColumns = ["id"],
            childColumns = ["scheduleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("scheduleId")]
)
data class ActivityScheduleFixedTimeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val scheduleId: Long,

    /**
     * Actual fixed time-of-day for this occurrence row.
     */
    val time: LocalTime,

    /**
     * Optional user-visible label such as "Morning" or "After lunch".
     */
    val label: String? = null,

    /**
     * Preserves user-entered ordering when multiple times exist.
     */
    val sortOrder: Int = 0
)