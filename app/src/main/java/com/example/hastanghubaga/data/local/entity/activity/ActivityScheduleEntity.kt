package com.example.hastanghubaga.data.local.entity.activity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.hastanghubaga.data.local.entity.supplement.ScheduleRecurrenceType
import com.example.hastanghubaga.data.local.entity.supplement.ScheduleTimingType
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

@Entity(
    tableName = "activity_schedules",
    foreignKeys = [
        ForeignKey(
            entity = ActivityEntity::class,
            parentColumns = ["id"],
            childColumns = ["activityId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("activityId")]
)
data class ActivityScheduleEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val activityId: Long,

    // -------------------------
    // Recurrence
    // -------------------------
    val recurrenceType: ScheduleRecurrenceType,
    val interval: Int,

    val weeklyDays: List<DayOfWeek>?,

    // -------------------------
    // Window
    // -------------------------
    val startDate: LocalDate,
    val endDate: LocalDate?,

    // -------------------------
    // Timing type
    // -------------------------
    val timingType: ScheduleTimingType,

    // -------------------------
    // State
    // -------------------------
    val isEnabled: Boolean = true
)