package com.example.hastanghubaga.data.local.entity.supplement

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalTime

@Entity(
    tableName = "supplement_schedule_fixed_times",
    foreignKeys = [
        ForeignKey(
            entity = SupplementScheduleEntity::class,
            parentColumns = ["id"],
            childColumns = ["scheduleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("scheduleId")]
)
data class SupplementScheduleFixedTimeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val scheduleId: Long,

    /**
     * Actual fixed time-of-day for this occurrence row.
     */
    val time: LocalTime,

    /**
     * Optional user-visible label such as "Morning" or "After workout".
     */
    val label: String? = null,

    /**
     * Preserves user-entered ordering when multiple times exist.
     */
    val sortOrder: Int = 0
)